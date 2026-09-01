package com.rahul.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class TenantDatabaseContext {

    private final JdbcTemplate jdbcTemplate;

    public TenantDatabaseContext(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setCurrentTenant(UUID tenantId) {

        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

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

    public UUID getCurrentTenant() {

        String tenantId =
                jdbcTemplate.queryForObject(
                        """
                        SELECT current_setting(
                            'app.current_tenant',
                            true
                        )
                        """,
                        String.class
                );

        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }

        return UUID.fromString(tenantId);
    }

    public void clearCurrentTenant() {

        jdbcTemplate.queryForObject(
                """
                SELECT set_config(
                    'app.current_tenant',
                    '',
                    true
                )
                """,
                String.class
        );
    }

    public void setRefreshTokenHash(String tokenHash) {

        Objects.requireNonNull(
                tokenHash,
                "tokenHash must not be null"
        );

        jdbcTemplate.queryForObject(
                """
                SELECT set_config(
                    'app.refresh_token_hash',
                    ?,
                    true
                )
                """,
                String.class,
                tokenHash
        );
    }

    public void clearRefreshTokenHash() {

        jdbcTemplate.queryForObject(
                """
                SELECT set_config(
                    'app.refresh_token_hash',
                    '',
                    true
                )
                """,
                String.class
        );
    }
}