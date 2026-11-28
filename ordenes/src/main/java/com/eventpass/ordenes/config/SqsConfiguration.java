package com.eventpass.ordenes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsConfiguration {

    @Bean(destroyMethod = "close")
    SqsClient sqsClient(@Value("${app.aws.region:us-east-1}") String region) {
        return SqsClient.builder()
                .region(Region.of(region))
                .build();
    }
}
