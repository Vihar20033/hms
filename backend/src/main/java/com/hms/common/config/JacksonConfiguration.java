package com.hms.common.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * Jackson configuration to register custom deserializers for handling flexible date/datetime formats.
 * This module is automatically picked up by Spring Boot's Jackson auto-configuration.
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public SimpleModule customDeserializerModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        return module;
    }
}
