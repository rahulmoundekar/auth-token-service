package com.rahul.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @Schema(
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {
}