package com.rahul.repository;

import com.rahul.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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
}