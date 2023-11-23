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
    public Region region(@Value("${aws-region:eu-west-1}") String awsRegion) {
        Region region = Region.of(awsRegion);
        if (Region.regions().contains(region)) return region; 
        return Region.EU_WEST_1;
    }

    @Bean
    public S3Client amazonS3(Region awsRegion) {
        return S3Client.builder().region(awsRegion).build();
    }

    @Bean
    public RekognitionClient amazonRekognition(Region awsRegion) {
        return RekognitionClient.builder().region(awsRegion).build();
    }
}
