package com.example.s3rekognition.controller;

import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;
import com.example.s3rekognition.TextRekognition;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


@SuppressWarnings({"JavadocLinkAsPlainText", "ResultOfMethodCallIgnored", "CallToPrintStackTrace"})
@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {
    private final S3Client s3Client;
    private final RekognitionClient rekognitionClient;
    private final TextRekognition textRekognition;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());

    @Autowired
    public RekognitionController(S3Client s3Client, RekognitionClient rekognitionClient, TextRekognition textRekognition, MeterRegistry meterRegistry) {
        this.s3Client = s3Client;
        this.rekognitionClient = rekognitionClient;
        this.textRekognition = textRekognition;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<Object> instructions() {
        logger.info("Served Instructions");

        @Language("html")
        String instructions = """
                <h1>Welcome to Candidate 2014 Java app!</h1>
                <h3>app has two main functions</h3><br>
                <div>1. Checking if people on images are using appropriate PPE equipments.</div><br>
                <div>2. Checking images of what text is written on them.</div><br><br>
                                
                <h3>To use the first one do the following:</h3><br>
                <div>- Command for docker run app:</div><br>
                <div>    curl http://localhost:8080/scan-ppe?bucketName=candidate2014</div><br>
                <div>- Command for AWS apprunner app:</div><br>
                <div>    curl &#60;AWS_apprunner_URL&#62;/scan-ppe?bucketName=<S3BUCKET></div><br><br>
                                
                <h3>To use the second one do the following:</h3>
                <div>- Have a jpg or png file locally.</div><br>
                <div>- There are some images laying in resources</div><br>
                <div>- Command for docker run app:</div><br>
                <div>    curl -F "files=@.\\src\\main\\resources\\images\\img1.jpg" http://localhost:8080/scan-text</div><br>
                <div>    OR</div><br>
                <div>    curl -F "files=@&#60;uri_to_image&#62;" http://localhost:8080/scan-text</div><br>
                <div>- Command for AWS apprunner app:</div><br>
                <div>    curl -F "files=@&#60;uri_to_image>" &#60;AWS_apprunner_URL&#62;/scan-text</div><br><br>
                    
                <div>NOTE: if that does not work, don't worry!</div><br>
                <div>I got a backup solution:</div><br>
                <div>- Use number between 1-3.</div><br>
                <div>- Command for docker run app:</div><br>
                <div>    curl http://localhost:8080/scan-text-backup/1</div><br>
                <div>- Command for AWS apprunner app:</div><br>
                <div>    curl &#60;AWS_apprunner_URL&#62;/scan-text-backup/1</div><br>
                """;
        return new ResponseEntity<>(instructions, HttpStatus.OK);
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Protective Gear Violations.
     * curl http://localhost:8080/scan-ppe?bucketName=candidate2014
     *
     * @param bucketName bucket name
     * @return json
     */
    @GetMapping(value = "/scan-ppe", consumes = "*/*", produces = "application/json")
    @Timed("time_avg_scan_ppe")
    public ResponseEntity<Object> scanForPPE(@RequestParam String bucketName) {
        try {
            // List all objects in the S3 bucket
            ListObjectsV2Response response = s3Client.listObjectsV2(builder -> builder.bucket(bucketName));
            List<S3Object> images = response.contents();
            List<PPEClassificationResponse> classificationResponses = new ArrayList<>();
            final float MIN_CONFIDENCE = 80f;

            for (S3Object image : images) {
                logger.info("scanning " + image.key());

                // Create request for Rekognition
                DetectProtectiveEquipmentRequest request = DetectProtectiveEquipmentRequest.builder().image(
                        Image.builder().s3Object(
                                software.amazon.awssdk.services.rekognition.model.S3Object.builder()
                                        .bucket(bucketName).name(image.key())
                                        .build()).build()
                ).summarizationAttributes(ProtectiveEquipmentSummarizationAttributes
                        .builder().minConfidence(MIN_CONFIDENCE).requiredEquipmentTypesWithStrings("FACE_COVER")
                        .build()).build();

                // Call Rekognition to detect PPE
                DetectProtectiveEquipmentResponse result = rekognitionClient.detectProtectiveEquipment(request);

                boolean violation = isViolation(result);
                if (violation) meterRegistry.counter("total_scan_ppe_violation").increment();
                logger.info("scanning " + image.key() + ", violation result " + violation);

                int personCount = result.persons().size();
                PPEClassificationResponse classification = new PPEClassificationResponse(image.key(), personCount, violation);
                classificationResponses.add(classification);
            }
            meterRegistry.counter("total_scan_ppe").increment();
            PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
            return ResponseEntity.ok(ppeResponse);
            
        } catch (Exception e) {
            meterRegistry.counter("crash_scan_ppe").increment();
            logger.severe("Error 500: AWS Rekognition");
            e.getMessage();
            e.printStackTrace();
            return new ResponseEntity<>("Error 500: AWS Rekognition", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Detects if the image has a protective gear violation for the FACE body part-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.
     *
     * @param result result
     * @return string
     */
    private static boolean isViolation(DetectProtectiveEquipmentResponse result) {
        return result.persons().stream()
                .flatMap(p -> p.bodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.name()
                                              .equals(BodyPart.FACE) && bodyPart.equipmentDetections()
                                              .isEmpty());
    }

    /**
     * Takes in pictures from POST and detects what Text is in them
     * curl -F "files=@.\src\main\resources\images\img1.jpg" http://localhost:8080/scan-text
     *
     * @param files sent inn as HTTP POST multipart/form-data
     * @return String of response from AWS Rekognition
     */
    @PostMapping(value = "/scan-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Timed("time_avg_scan_text")
    public ResponseEntity<Object> scanTextOnImage(@RequestParam("files") MultipartFile files) {
        if (files.isEmpty()) {
            logger.warning("Error 400: No file received");
            return new ResponseEntity<>("Error 400: No file received", HttpStatus.BAD_REQUEST);
        }

        StringBuilder response = new StringBuilder();
        try {
            logger.info("Scanning file " + files.getName() + " from POST");
            response.append(textRekognition.detectTextLabels(files.getInputStream().readAllBytes()));

        } catch (Exception e) {
            meterRegistry.counter("crash_scan_text").increment();
            logger.severe("Error 500: AWS Rekognition");
            e.getMessage();
            e.printStackTrace();
            return new ResponseEntity<>("Error 500: AWS Rekognition", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        meterRegistry.counter("total_scan_text").increment();
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    /**
     * Takes images from backup S3 and send them to AWS rekognition
     * curl http://localhost:8080/scan-text-backup/1
     *
     * @param id needs to be between 1-3
     * @return String of response from AWS Rekognition
     */
    @GetMapping("/scan-text-backup/{id}")
    @Timed("time_avg_scan_text_backup")
    public ResponseEntity<Object> scanTextOnImageBackup(@PathVariable int id) {
        if (id < 1 || id > 3) {
            logger.warning("Error 400: ID outside of range 1 - 3");
            return new ResponseEntity<>("Error 400: ID outside of range 1 - 3", HttpStatus.BAD_REQUEST);
        }

        String bucketName = "candidate2014-text";
        String key = null;
        switch (id) {
            case 1 -> key = "img1.jpg";
            case 2 -> key = "img2.jpg";
            case 3 -> key = "img3.jpg";
        }

        String response;
        try {
//            Get file from S3 bucket
            logger.info("Scanning file " + key + " from S3 " + bucketName);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
            ResponseInputStream<GetObjectResponse> s3object = s3Client.getObject(getObjectRequest);

//            Do AWS Rekognition text scan
            response = textRekognition.detectTextLabels(s3object.readAllBytes());
        } catch (Exception e) {
            meterRegistry.counter("crash_scan_text_backup").increment();
            logger.severe("Error 500: AWS Rekognition");
            e.getMessage();
            e.printStackTrace();
            return new ResponseEntity<>("Error 500: AWS Rekognition", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        meterRegistry.counter("total_scan_text_backup").increment();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        
    }
}