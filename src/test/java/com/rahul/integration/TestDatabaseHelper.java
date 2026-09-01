package com.rahul.integration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public final class TestDatabaseHelper {

    private TestDatabaseHelper() {
    }

    public static JdbcTemplate adminJdbcTemplate(
            String jdbcUrl
    ) {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setDriverClassName(
                "org.postgresql.Driver"
        );

        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername("postgres");
        dataSource.setPassword("root");

        return new JdbcTemplate(dataSource);
    }
}