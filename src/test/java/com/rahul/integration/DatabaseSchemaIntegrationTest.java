package com.rahul.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseSchemaIntegrationTest
        extends PostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void requiredTablesShouldExist() {

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN (
                              'tenants',
                              'users',
                              'roles',
                              'permissions',
                              'user_roles',
                              'role_permissions',
                              'refresh_tokens'
                          )
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(7);
    }

    @Test
    void tenantTablesShouldHaveRlsEnabled() {

        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(
                        """
                        SELECT
                            relname,
                            relrowsecurity,
                            relforcerowsecurity
                        FROM pg_class
                        WHERE relname IN (
                            'users',
                            'roles',
                            'refresh_tokens'
                        )
                        ORDER BY relname
                        """
                );

        assertThat(rows)
                .hasSize(3);

        rows.forEach(row -> {
            assertThat(row.get("relrowsecurity"))
                    .isEqualTo(true);

            assertThat(row.get("relforcerowsecurity"))
                    .isEqualTo(true);
        });
    }

    @Test
    void tenantPoliciesShouldExist() {

        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM pg_policies
                        WHERE schemaname = 'public'
                          AND tablename IN (
                              'users',
                              'roles',
                              'refresh_tokens'
                          )
                        """,
                        Integer.class
                );

        assertThat(count)
                .isGreaterThanOrEqualTo(3);
    }
}