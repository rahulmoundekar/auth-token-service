package com.rahul.integration;

import com.rahul.AuthTokenServiceApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(
        classes = AuthTokenServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class PostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("auth_service")
                    .withUsername("postgres")
                    .withPassword("root")
                    .withInitScript("init-auth-app.sql");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                () -> "auth_app"
        );

        registry.add(
                "spring.datasource.password",
                () -> "root"
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "validate"
        );

        registry.add(
                "spring.flyway.enabled",
                () -> true
        );

        registry.add(
                "spring.jpa.properties.hibernate.jdbc.time_zone",
                () -> "UTC"
        );
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