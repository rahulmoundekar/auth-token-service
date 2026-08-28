package com.rahul.security;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@RequiredArgsConstructor
public class TenantTransactionAspect {

    private final JdbcTemplate jdbcTemplate;

    @Before(
            "@annotation(org.springframework.transaction.annotation.Transactional)"
    )
    public void applyTenantContext() {

        if (!TransactionSynchronizationManager
                .isActualTransactionActive()) {
            return;
        }

        var tenantId =
                TenantContext.getTenantId();

        if (tenantId == null) {
            return;
        }

        jdbcTemplate.query(
                "select set_config(" +
                        "'app.current_tenant', ?, true)",
                ps -> ps.setString(
                        1,
                        tenantId.toString()
                ),
                rs -> {
                }
        );
    }
}