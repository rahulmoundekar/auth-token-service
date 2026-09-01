package com.rahul.controller;

import com.rahul.dto.*;
import com.rahul.service.AuthenticationService;
import com.rahul.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(
        name = "Authentication",
        description = "User login/logout and token lifecycle operations"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "Authenticate user",
            description = """
                Authenticates a user within the requested tenant and
                returns a JWT access token and a rotating refresh token.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
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

    @Operation(
            summary = "Rotate refresh token",
            description = """
                Validates the supplied refresh token, revokes the old
                token and issues a new access token and refresh token.
                Refresh-token reuse is rejected.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token rotated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid, expired or reused refresh token"
            )
    })
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

    @Operation(
            summary = "Logout",
            description = """
                Revokes the refresh-token family associated with the
                supplied refresh token.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid refresh token"
            )
    })
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