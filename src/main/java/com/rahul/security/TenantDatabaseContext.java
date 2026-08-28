package com.rahul.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantDatabaseContext {

    private final JdbcTemplate jdbcTemplate;

    public void setCurrentTenant(UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        jdbcTemplate.queryForObject(
                """
                SELECT set_config(
                    'app.current_tenant',
                    ?,
                    true
                )
                """,
                String.class,
                tenantId.toString()
        );
    }
}