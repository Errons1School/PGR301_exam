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

    @Value("${aws-region: eu-west-1}")
    private String awsRegion;
    
    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        Region region;
        try {
            region = Region.of(awsRegion);
        } catch (Exception error) {
            region = Region.EU_WEST_1;
        }
        return CloudWatchAsyncClient.builder().region(region).build();
    }

    @Bean
    public MeterRegistry getMeterRegistry() {
        CloudWatchConfig cloudWatchConfig = setupCloudWatchConfig();
        return new CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, cloudWatchAsyncClient());
    }

    private CloudWatchConfig setupCloudWatchConfig() {
        return new CloudWatchConfig() {
            private final Map<String, String> configuration = Map.of("cloudwatch.namespace", "candidate2014-dashboard", "cloudwatch.step", Duration.ofSeconds(5).toString());

            @Override
            public String get(String key) {
                return configuration.get(key);
            }
        };
    }

}
