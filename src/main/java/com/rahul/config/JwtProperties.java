package com.rahul.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank
        @Size(min = 32)
        String secret,

        @NotBlank
        String issuer,
        long accessTokenExpiration
) {
}