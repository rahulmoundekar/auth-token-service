package com.rahul.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authTokenServiceOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Auth Token Service API")
                                .description(
                                        """
                                        Multi-tenant authentication and authorization
                                        service providing JWT access tokens,
                                        rotating refresh tokens, RBAC and
                                        PostgreSQL Row-Level Security.
                                        """
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Rahul Moundekar")
                                )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                                .addSecuritySchemes(
                                        "tenantId",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.HEADER)
                                                .name("X-Tenant-Id")
                                )
                );
    }
}