package com.rahul.repository;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenLookup(
        UUID id,
        UUID userId,
        UUID tenantId,
        String tokenHash,
        Instant expiresAt,
        boolean revoked,
        Instant revokedAt,
        String tokenFamily,
        long version,
        Instant createdAt
) {
}