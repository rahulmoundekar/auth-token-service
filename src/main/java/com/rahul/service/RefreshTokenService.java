package com.rahul.service;

import com.rahul.config.RefreshTokenProperties;
import com.rahul.entity.RefreshToken;
import com.rahul.entity.User;
import com.rahul.repository.RefreshTokenRepository;
import com.rahul.repository.UserRoleRepository;
import com.rahul.security.JwtService;
import com.rahul.security.RefreshTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenProperties refreshTokenProperties;
    private final JwtService jwtService;

    @Transactional
    public CreatedRefreshToken create(User user) {

        String rawToken =
                refreshTokenGenerator.generate();

        String tokenHash =
                refreshTokenGenerator.hash(
                        rawToken
                );

        Instant expiresAt =
                Instant.now().plusMillis(
                        refreshTokenProperties.expiration()
                );

        RefreshToken refreshToken =
                new RefreshToken(
                        user,
                        user.getTenant(),
                        tokenHash,
                        expiresAt
                );

        refreshTokenRepository.save(
                refreshToken
        );

        return new CreatedRefreshToken(
                rawToken,
                expiresAt
        );
    }

    @Transactional
    public RefreshResult rotate(
            String rawRefreshToken
    ) {

        String hash =
                refreshTokenGenerator.hash(
                        rawRefreshToken
                );

        RefreshToken existing =
                refreshTokenRepository
                        .findByTokenHash(hash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid refresh token"
                                )
                        );

        /*
         * Reuse detection:
         *
         * If an already-revoked refresh token is presented,
         * revoke all currently active refresh tokens for that user.
         */
        if (existing.isRevoked()) {

            refreshTokenRepository
                    .revokeAllActiveByUserId(
                            existing.getUser().getId()
                    );

            throw new IllegalArgumentException(
                    "Refresh token reuse detected"
            );
        }

        if (existing.getExpiresAt()
                .isBefore(Instant.now())) {

            existing.revoke();

            throw new IllegalArgumentException(
                    "Refresh token expired"
            );
        }

        User user =
                existing.getUser();

        existing.revoke();

        List<String> roles =
                userRoleRepository
                        .findByUserId(
                                user.getId()
                        )
                        .stream()
                        .map(userRole ->
                                userRole
                                        .getRole()
                                        .getName()
                        )
                        .distinct()
                        .toList();

        String accessToken =
                jwtService.generateAccessToken(
                        user.getId(),
                        user.getTenant().getId(),
                        roles
                );

        CreatedRefreshToken newRefreshToken =
                create(user);

        return new RefreshResult(
                accessToken,
                newRefreshToken.rawToken(),
                newRefreshToken.expiresAt(),
                900
        );
    }

    public record CreatedRefreshToken(
            String rawToken,
            Instant expiresAt
    ) {
    }

    public record RefreshResult(
            String accessToken,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            long accessTokenExpiresIn
    ) {
    }
}