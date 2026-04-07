package com.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableMethodSecurity
@EnableAsync
@EnableRetry
@EntityScan(basePackages = "com.hms")
@EnableJpaRepositories(basePackages = "com.hms")
@ConfigurationPropertiesScan(basePackages = "com.hms")
@SpringBootApplication
public class HospitalManagementSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(HospitalManagementSystemApplication.class, args);
	}
}
