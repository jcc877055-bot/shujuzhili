package com.example.datagov.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.databind.DeserializationFeature;
@Configuration public class WebConfig {
 @Bean Jackson2ObjectMapperBuilderCustomizer strictJson() { return builder->builder.featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); }
}
