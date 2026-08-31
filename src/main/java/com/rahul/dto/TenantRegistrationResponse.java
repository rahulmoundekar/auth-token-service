package com.rahul.dto;

import java.time.Instant;
import java.util.UUID;

public record TenantRegistrationResponse(
        UUID id,
        String name,
        Instant createdAt
) {
}