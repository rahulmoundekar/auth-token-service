package com.rahul.repository;

import com.rahul.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    @Modifying
    @Query("""
    update RefreshToken r
       set r.revoked = true,
           r.revokedAt = CURRENT_TIMESTAMP
     where r.user.id = :userId
       and r.revoked = false
""")
    int revokeAllActiveByUserId(UUID userId);

    @Modifying
    @Query("""
    update RefreshToken r
       set r.revoked = true,
           r.revokedAt = CURRENT_TIMESTAMP
     where r.tokenFamily = :tokenFamily
       and r.revoked = false
""")
    int revokeTokenFamily(String tokenFamily);

    @Query(
            value = """
            SELECT
                id,
                user_id,
                tenant_id,
                token_hash,
                expires_at,
                revoked,
                revoked_at,
                token_family,
                version,
                created_at
            FROM refresh_tokens
            WHERE token_hash = :tokenHash
            """,
            nativeQuery = true
    )
    Optional<RefreshTokenLookup> findForRefreshBootstrap(
            @Param("tokenHash") String tokenHash
    );

    @Modifying
    @Query(
            value = """
            UPDATE refresh_tokens
            SET revoked = true,
                revoked_at = CURRENT_TIMESTAMP,
                version = version + 1
            WHERE id = :id
              AND version = :version
              AND revoked = false
            """,
            nativeQuery = true
    )
    int revokeIfActive(
            @Param("id") UUID id,
            @Param("version") long version
    );
}