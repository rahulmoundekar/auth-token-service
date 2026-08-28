package com.rahul;

import com.rahul.config.JwtProperties;
import com.rahul.config.RefreshTokenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, RefreshTokenProperties.class})
public class AuthTokenServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthTokenServiceApplication.class, args);
    }

}
