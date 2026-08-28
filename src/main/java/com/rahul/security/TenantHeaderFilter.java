package com.rahul.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantHeaderFilter
        extends OncePerRequestFilter {

    private static final String TENANT_HEADER =
            "X-Tenant-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String tenantHeader =
                request.getHeader(TENANT_HEADER);

        try {

            if (tenantHeader != null &&
                    !tenantHeader.isBlank()) {

                TenantContext.setTenantId(
                        UUID.fromString(tenantHeader)
                );
            }

            filterChain.doFilter(
                    request,
                    response
            );

        } finally {

            TenantContext.clear();
        }
    }
}