package com.example.skillseeker_be.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate geminiRestTemplate(GeminiProperties properties) {
        return new RestTemplateBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .readTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }
}
