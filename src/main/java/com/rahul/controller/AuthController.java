package com.rahul.controller;

import com.rahul.dto.*;
import com.rahul.service.AuthenticationService;
import com.rahul.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

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

    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(
            @Valid
            @RequestBody
            RefreshTokenRequest request
    ) {

        var result =
                refreshTokenService.rotate(
                        request.refreshToken()
                );

        return new RefreshTokenResponse(
                result.accessToken(),
                result.refreshToken(),
                "Bearer",
                result.accessTokenExpiresIn()
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid
            @RequestBody
            LogoutRequest request
    ) {

        refreshTokenService.logout(
                request.refreshToken()
        );
    }
}