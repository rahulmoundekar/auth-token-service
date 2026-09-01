package com.rahul.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API error response")
public record ApiErrorResponse(

        @Schema(
                description = "Time at which the error occurred",
                example = "2026-09-01T12:00:00Z"
        )
        Instant timestamp,

        @Schema(
                example = "401"
        )
        int status,

        @Schema(
                example = "Invalid refresh token"
        )
        String error,

        @Schema(
                example = "Invalid refresh token"
        )
        String message,

        @Schema(
                example = "/api/auth/refresh"
        )
        String path
) {
}