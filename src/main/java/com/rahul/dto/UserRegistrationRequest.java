package com.rahul.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserRegistrationRequest(

        @NotNull(message = "tenantId is required")
        UUID tenantId,

        @NotBlank(message = "username is required")
        @Size(min = 3, max = 100,
                message = "username must be between 3 and 100 characters")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 12, max = 200,
                message = "password must be between 12 and 200 characters")
        String password
) {
}