package com.example.s3rekognition.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AmazonConfig {

    @Bean
    public S3Client amazonS3(@Value("${aws-region:eu-west-1}") String awsRegion) {
        return S3Client.builder().region(Region.of(awsRegion)).build();
    }

    @Bean
    public RekognitionClient amazonRekognition(@Value("${aws-region:eu-west-1}") String awsRegion) {
        return RekognitionClient.builder().region(Region.of(awsRegion)).build();
    }
}
