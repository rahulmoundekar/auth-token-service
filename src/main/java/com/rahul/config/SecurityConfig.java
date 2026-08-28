package com.rahul.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Temporary endpoint for Step 5 testing
                        .requestMatchers(
                                "/api/test/tenant/**",
                                "/api/auth/tenants/**",
                                "/api/auth/**"
                        )
                        .permitAll()

                        // Health endpoint
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/liveness",
                                "/actuator/health/readiness"
                        )
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }
}