package com.rahul.service;

import com.rahul.dto.LoginRequest;
import com.rahul.dto.LoginResponse;
import com.rahul.entity.User;
import com.rahul.repository.UserRepository;
import com.rahul.repository.UserRoleRepository;
import com.rahul.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(
            LoginRequest request
    ) {

        if (request.tenantId() == null) {
            throw new IllegalArgumentException(
                    "tenantId is required"
            );
        }

        User user =
                userRepository
                        .findByTenantIdAndUsername(
                                request.tenantId(),
                                request.username()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid credentials"
                                )
                        );

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "User is disabled"
            );
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Invalid credentials"
            );
        }

        List<String> roles =
                userRoleRepository
                        .findByUserId(
                                user.getId()
                        )
                        .stream()
                        .map(userRole ->
                                userRole
                                        .getRole()
                                        .getName()
                        )
                        .distinct()
                        .toList();

        String token =
                jwtService.generateAccessToken(
                        user.getId(),
                        user.getTenant().getId(),
                        roles
                );

        long expiresIn =
                900;

        return new LoginResponse(
                token,
                "Bearer",
                expiresIn
        );
    }
}