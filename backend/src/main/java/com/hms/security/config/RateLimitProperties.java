package com.hms.security.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "hms.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    @Min(1)
    private long capacity = 120;

    @Min(1)
    private long refillTokens = 120;

    @Min(1)
    private long refillMinutes = 1;
}
