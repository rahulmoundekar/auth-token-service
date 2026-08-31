package com.rahul.integration;

import com.rahul.AuthTokenServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = AuthTokenServiceApplication.class
)
class AuthTokenServiceIntegrationTest
        extends PostgresIntegrationTest {

    @Test
    void contextLoads() {
    }
}