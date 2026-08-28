package com.rahul.security;

import com.rahul.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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

        var now = new java.util.Date();

        var expiration =
                new java.util.Date(
                        now.getTime()
                                + jwtProperties.accessTokenExpiration()
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
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey())
                .compact();
    }

    public Claims parseAndValidate(String token) {

        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {

        Claims claims =
                parseAndValidate(token);

        return UUID.fromString(
                claims.getSubject()
        );
    }

    public UUID extractTenantId(String token) {

        Claims claims =
                parseAndValidate(token);

        String tenantId =
                claims.get("tenant_id", String.class);

        return UUID.fromString(tenantId);
    }

    public List<String> extractRoles(String token) {

        Claims claims =
                parseAndValidate(token);

        return claims.get("roles", List.class);
    }

    public boolean isValid(String token) {

        try {
            parseAndValidate(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}