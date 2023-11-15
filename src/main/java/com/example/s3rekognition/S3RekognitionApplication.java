package com.example.s3rekognition;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class S3RekognitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(S3RekognitionApplication.class, args);
    }
    
    @Bean
    public AmazonS3 amazonS3(@Value("${aws-region: eu-west-1}") String awsRegion) {
        return AmazonS3ClientBuilder.standard().withRegion(awsRegion).build();
    }
    
    @Bean
    public AmazonRekognition amazonRekognition(@Value("${aws-region: eu-west-1}") String awsRegion) {
        return AmazonRekognitionClientBuilder.standard().withRegion(awsRegion).build();
    }
    
}
