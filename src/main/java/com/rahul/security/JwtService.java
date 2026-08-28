package com.rahul.security;

import com.rahul.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {

        return Keys.hmacShaKeyFor(
                jwtProperties.secret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(
            UUID userId,
            UUID tenantId,
            List<String> roles
    ) {

        Instant now = Instant.now();

        Instant expiration =
                now.plusMillis(
                        jwtProperties.accessTokenExpiration()
                );

        return Jwts.builder()
                .subject(userId.toString())

                .claim(
                        "tenant_id",
                        tenantId.toString()
                )

                .claim(
                        "roles",
                        roles
                )

                .issuedAt(
                        Date.from(now)
                )

                .expiration(
                        Date.from(expiration)
                )

                .signWith(
                        signingKey()
                )

                .compact();
    }
}