package com.rahul.service;

import com.rahul.config.JwtProperties;
import com.rahul.dto.LoginRequest;
import com.rahul.dto.LoginResponse;
import com.rahul.entity.User;
import com.rahul.exception.AuthenticationException;
import com.rahul.repository.RefreshTokenRepository;
import com.rahul.repository.UserRepository;
import com.rahul.repository.UserRoleRepository;
import com.rahul.security.JwtService;
import com.rahul.security.TenantDatabaseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final TenantDatabaseContext tenantDatabaseContext;

    public LoginResponse login(LoginRequest request) {

        UUID tenantId = request.tenantId();

        if (tenantId == null) {
            throw new AuthenticationException("tenantId is required");
        }

        tenantDatabaseContext.setCurrentTenant(tenantId);

        User user = userRepository.findByTenantIdAndUsername(request.tenantId(), request.username()).orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new AuthenticationException("User is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        List<String> roles = userRoleRepository.findByUserId(user.getId()).stream().map(userRole -> userRole.getRole().getName()).distinct().toList();

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getTenant().getId(), roles);

        String tokenFamily = UUID.randomUUID().toString();

        RefreshTokenService.CreatedRefreshToken refreshToken = refreshTokenService.create(user, tokenFamily);

        return new LoginResponse(accessToken, refreshToken.rawToken(), "Bearer", accessTokenExpiresInSeconds());
    }

    @Transactional
    public void logoutAll(UUID userId) {

        refreshTokenRepository.revokeAllActiveByUserId(userId);
    }

    public long accessTokenExpiresInSeconds() {
        return jwtProperties.accessTokenExpiration() / 1000;
    }

}