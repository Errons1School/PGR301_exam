package com.example.s3rekognition.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Map;

@Configuration
public class MetricsConfig {

    @Value("${aws-dashboard:candidate2014-dashboard}") 
    String awsDashboard;
    
    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient(Region awsRegion) {
        return CloudWatchAsyncClient.builder().region(awsRegion).build();
    }

    @Bean
    public MeterRegistry getMeterRegistry(Region awsRegion) {
        CloudWatchConfig cloudWatchConfig = setupCloudWatchConfig();
        return new CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, cloudWatchAsyncClient(awsRegion));
    }

    private CloudWatchConfig setupCloudWatchConfig() {
        return new CloudWatchConfig() {
            private final Map<String, String> configuration = Map.of("cloudwatch.namespace", awsDashboard, "cloudwatch.step", Duration.ofSeconds(5).toString());

            @Override
            public String get(String key) {
                return configuration.get(key);
            }
        };
    }

}
