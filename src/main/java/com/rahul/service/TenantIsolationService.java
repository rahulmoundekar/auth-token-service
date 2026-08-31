package com.rahul.service;

import com.rahul.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TenantIsolationService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public List<Map<String, Object>> findVisibleUsers() {

        var tenantId =
                TenantContext.requireTenantId();

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

        return jdbcTemplate.queryForList(
                """
                SELECT
                    id,
                    tenant_id,
                    username,
                    enabled
                FROM users
                ORDER BY username
                """
        );
    }
}