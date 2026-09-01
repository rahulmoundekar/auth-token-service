package com.rahul.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LoginRequest(

        @Schema(
                example = "11111111-1111-1111-1111-111111111111"
        )
        @NotNull(message = "tenantId is required")
        UUID tenantId,

        @Schema(example = "alice")
        @NotBlank(message = "username is required")
        String username,

        @Schema(
                example = "Password@123",
                format = "password"
        )
        @NotBlank(message = "password is required")
        String password
) {
}