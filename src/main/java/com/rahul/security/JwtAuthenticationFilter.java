package com.rahul.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorization.substring(7);

        try {

            Claims claims =
                    jwtService.parseAndValidate(token);

            UUID userId =
                    UUID.fromString(
                            claims.getSubject()
                    );

            UUID tenantId =
                    UUID.fromString(
                            claims.get(
                                    "tenant_id",
                                    String.class
                            )
                    );
            TenantContext.setTenantId(tenantId);

            List<String> roles =
                    claims.get(
                            "roles",
                            List.class
                    );

            var authorities =
                    roles.stream()
                            .map(role ->
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            )
                            .toList();

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new JwtAuthenticationDetails(
                            userId,
                            tenantId,
                            roles
                    )
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (Exception e) {

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }

        try {

            filterChain.doFilter(
                    request,
                    response
            );

        } finally {

            SecurityContextHolder.clearContext();
            TenantContext.clear();
        }
    }
}