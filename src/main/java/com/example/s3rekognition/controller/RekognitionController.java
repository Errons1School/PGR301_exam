package com.example.s3rekognition.controller;

import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;
import com.example.s3rekognition.TextRekognition;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
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


@SuppressWarnings("JavadocLinkAsPlainText")
@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {
    private final S3Client s3Client;
    private final RekognitionClient rekognitionClient;
    private final TextRekognition textRekognition;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());
    private static int totalPPEScan = 12;
    private static int totalTextScan = 21;
    private static int counter = 0;

    @Autowired
    public RekognitionController(S3Client s3Client, RekognitionClient rekognitionClient, TextRekognition textRekognition, MeterRegistry meterRegistry) {
        this.s3Client = s3Client;
        this.rekognitionClient = rekognitionClient;
        this.textRekognition = textRekognition;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/")
    public ResponseEntity<Object> helloWorld() {
        logger.info("Hello world " + counter++);
        meterRegistry.counter("hello_world").increment();
        totalPPEScan++;
        totalTextScan++;
        return new ResponseEntity<>(("Hello World " + counter), HttpStatus.OK);
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
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForPPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Response response = s3Client.listObjectsV2(builder -> builder.bucket(bucketName));
        List<S3Object> images = response.contents();

        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();
        final float MIN_CONFIDENCE = 80f;

        for (S3Object image : images) {
            logger.info("scanning " + image.key());

            // Create request for Rekognition
            DetectProtectiveEquipmentRequest request = DetectProtectiveEquipmentRequest.builder().image(Image.builder().s3Object(software.amazon.awssdk.services.rekognition.model.S3Object.builder().bucket(bucketName).name(image.key()).build()).build()).summarizationAttributes(ProtectiveEquipmentSummarizationAttributes.builder().minConfidence(MIN_CONFIDENCE).requiredEquipmentTypesWithStrings("FACE_COVER").build()).build();

            // Call Rekognition to detect PPE
            DetectProtectiveEquipmentResponse result = rekognitionClient.detectProtectiveEquipment(request);

            boolean violation = isViolation(result); // Assuming isViolation() is implemented elsewhere
            logger.info("scanning " + image.key() + ", violation result " + violation);

            int personCount = result.persons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.key(), personCount, violation);
            classificationResponses.add(classification);
            // totalPPEScan++; // Assuming totalPPEScan is a counter variable defined elsewhere
        }

        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
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
    @ResponseBody
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
            logger.severe("Error 500: AWS Rekognition");
            e.getMessage();
            e.printStackTrace();
            return new ResponseEntity<>("Error 500: AWS Rekognition", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    /**
     * Takes images from backup S3 and send them to AWS rekognition
     * curl http://localhost:8080/scan-text-backup/1
     * 
     * @param id needs to be between 1-4
     * @return String of response from AWS Rekognition
     */
    @GetMapping("/scan-text-backup/{id}")
    public ResponseEntity<Object> scanTextOnImageBackup(@PathVariable int id) {
        if (id < 1 || id > 4) {
            logger.warning("Error 400: ID outside of range 1 - 4");
            return new ResponseEntity<>("Error 400: ID outside of range 1 - 4", HttpStatus.BAD_REQUEST);
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
            logger.severe("Error 500: AWS Rekognition");
            e.getMessage();
            e.printStackTrace();
            return new ResponseEntity<>("Error 500: AWS Rekognition", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Gauge.builder("PPE_scan_count", totalPPEScan, Integer::intValue).register(meterRegistry);
        Gauge.builder("Text_scan_count", totalTextScan, Integer::intValue).register(meterRegistry);
    }
}