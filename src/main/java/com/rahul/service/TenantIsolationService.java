package com.rahul.service;

import com.rahul.security.TenantContext;
import com.rahul.security.TenantDatabaseContext;
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
    private final TenantDatabaseContext tenantDatabaseContext;

    @Transactional
    public List<Map<String, Object>> findVisibleUsers() {

        var tenantId =
                TenantContext.requireTenantId();

        tenantDatabaseContext.setCurrentTenant(
                tenantId
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