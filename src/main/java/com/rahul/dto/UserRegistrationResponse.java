package com.rahul.dto;

import java.util.UUID;

public record UserRegistrationResponse(
        UUID id,
        UUID tenantId,
        String username,
        boolean enabled
) {
}