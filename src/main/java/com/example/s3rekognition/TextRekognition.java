package com.example.s3rekognition;

import org.intellij.lang.annotations.Language;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

import java.util.List;

@Component
public class TextRekognition {

    private final RekognitionClient rekognitionClient;

    @Autowired
    public TextRekognition(RekognitionClient rekognitionClient) {
        this.rekognitionClient = rekognitionClient;
    }

    /**
     * Takes in an input stream from a file and sends that file to AWS for text detection
     * Based on Java V2 URL: <a href="https://docs.aws.amazon.com/rekognition/latest/dg/text-detecting-text-procedure.html">AWS Docs</a>
     *
     * @param inputStream from a file
     * @return String result from AWS text detection
     */
    public String detectTextLabels(byte[] inputStream) throws RekognitionException {
        SdkBytes sourceBytes = SdkBytes.fromByteArray(inputStream);
        Image image = Image.builder().bytes(sourceBytes).build();

        DetectTextRequest textRequest = DetectTextRequest.builder().image(image).build();

        DetectTextResponse textResponse = rekognitionClient.detectText(textRequest);
        List<TextDetection> textCollection = textResponse.textDetections();

        JSONArray builder = new JSONArray();
        for (TextDetection text : textCollection) {
            builder.put(new JSONObject()
                    .put("detected", text.detectedText())
                    .put("confidence", text.confidence().toString())
                    .put("id", text.id())
                    .put("parentId", text.parentId())
                    .put("type", text.type())
             );
        }

        return builder.toString();
    }
}

