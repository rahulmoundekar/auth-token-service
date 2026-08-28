package com.rahul.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TenantTransactionExecutor {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public <T> T execute(
            UUID tenantId,
            Supplier<T> operation
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        return transactionTemplate.execute(status -> {

            setTenant(tenantId);

            return operation.get();
        });
    }

    private void setTenant(UUID tenantId) {

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