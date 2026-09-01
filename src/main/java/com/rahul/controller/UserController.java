package com.rahul.controller;

import com.rahul.security.TenantContext;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public String profile(
            Authentication authentication
    ) {

        return "User profile for: "
                + authentication.getName();
    }

    @GetMapping("/tenant")
    public String tenant() {

        return TenantContext
                .requireTenantId()
                .toString();
    }
}