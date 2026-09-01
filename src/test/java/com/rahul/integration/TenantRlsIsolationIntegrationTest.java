package com.rahul.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenantRlsIsolationIntegrationTest
        extends PostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate adminJdbcTemplate;

    private UUID tenantA;

    private UUID tenantB;

    @BeforeEach
    void setup() {

        adminJdbcTemplate =
                TestDatabaseHelper.adminJdbcTemplate(
                        POSTGRES.getJdbcUrl()
                );

        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();

        /*
         * Cleanup from previous test execution.
         */
        adminJdbcTemplate.update(
                "DELETE FROM users"
        );

        adminJdbcTemplate.update(
                "DELETE FROM tenants"
        );

        /*
         * Create two tenants.
         */
        adminJdbcTemplate.update(
                """
                INSERT INTO tenants(
                    id,
                    name
                )
                VALUES (?, ?), (?, ?)
                """,
                tenantA,
                "Tenant A",
                tenantB,
                "Tenant B"
        );

        /*
         * Create user Alice in Tenant A.
         */
        adminJdbcTemplate.update(
                """
                INSERT INTO users(
                    id,
                    tenant_id,
                    username,
                    password_hash,
                    enabled
                )
                VALUES (?, ?, ?, ?, true)
                """,
                UUID.randomUUID(),
                tenantA,
                "alice",
                "test-hash"
        );

        /*
         * Create user Bob in Tenant B.
         */
        adminJdbcTemplate.update(
                """
                INSERT INTO users(
                    id,
                    tenant_id,
                    username,
                    password_hash,
                    enabled
                )
                VALUES (?, ?, ?, ?, true)
                """,
                UUID.randomUUID(),
                tenantB,
                "bob",
                "test-hash"
        );
    }

    @Test
    @Transactional
    void tenantAShouldSeeOnlyTenantAUsers() {

        setTenant(tenantA);

        List<String> usernames =
                jdbcTemplate.queryForList(
                        """
                        SELECT username
                        FROM users
                        ORDER BY username
                        """,
                        String.class
                );

        assertThat(usernames)
                .containsExactly("alice");
    }

    @Test
    @Transactional
    void tenantBShouldSeeOnlyTenantBUsers() {

        setTenant(tenantB);

        List<String> usernames =
                jdbcTemplate.queryForList(
                        """
                        SELECT username
                        FROM users
                        ORDER BY username
                        """,
                        String.class
                );

        assertThat(usernames)
                .containsExactly("bob");
    }

    @Test
    @Transactional
    void tenantAShouldNotSeeTenantBUsers() {

        setTenant(tenantA);

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM users
                        WHERE username = 'bob'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isZero();
    }

    @Test
    @Transactional
    void tenantBShouldNotSeeTenantAUsers() {

        setTenant(tenantB);

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM users
                        WHERE username = 'alice'
                        """,
                        Integer.class
                );

        assertThat(count)
                .isZero();
    }

    private void setTenant(
            UUID tenantId
    ) {

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