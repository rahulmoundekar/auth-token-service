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

        byte[] keyBytes =
                jwtProperties.secret()
                        .getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
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
                .issuer(jwtProperties.issuer())
                .id(UUID.randomUUID().toString())
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

    public Claims parseAndValidate(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(
                        jwtProperties.issuer()
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}