package com.rahul.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record LoginRequest(

        UUID tenantId,

        @NotBlank
        String username,

        @NotBlank
        String password
) {
}