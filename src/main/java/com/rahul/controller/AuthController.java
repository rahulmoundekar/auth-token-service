package com.rahul.controller;

import com.rahul.dto.LoginRequest;
import com.rahul.dto.LoginResponse;
import com.rahul.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {

        return authenticationService.login(
                request
        );
    }
}