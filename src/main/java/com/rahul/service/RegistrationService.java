package com.rahul.service;

import com.rahul.dto.TenantRegistrationRequest;
import com.rahul.dto.UserRegistrationRequest;
import com.rahul.entity.Role;
import com.rahul.entity.Tenant;
import com.rahul.entity.User;
import com.rahul.entity.UserRole;
import com.rahul.repository.RoleRepository;
import com.rahul.repository.TenantRepository;
import com.rahul.repository.UserRepository;
import com.rahul.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Tenant registerTenant(
            TenantRegistrationRequest request
    ) {

        if (tenantRepository.existsByName(request.name())) {
            throw new IllegalArgumentException(
                    "Tenant already exists"
            );
        }

        Tenant tenant =
                new Tenant(request.name());

        return tenantRepository.save(tenant);
    }

    @Transactional
    public User registerUser(
            UserRegistrationRequest request
    ) {

        if (request.tenantId() == null) {
            throw new IllegalArgumentException(
                    "tenantId is required"
            );
        }

        Tenant tenant =
                tenantRepository.findById(
                        request.tenantId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found"
                        )
                );

        if (userRepository.existsByTenantIdAndUsername(
                request.tenantId(),
                request.username()
        )) {
            throw new IllegalArgumentException(
                    "Username already exists for tenant"
            );
        }

        String passwordHash =
                passwordEncoder.encode(
                        request.password()
                );

        User user =
                new User(
                        tenant,
                        request.username(),
                        passwordHash
                );

        User savedUser =
                userRepository.save(user);

        Role userRole =
                roleRepository
                        .findByTenantIdAndName(
                                tenant.getId(),
                                "USER"
                        )
                        .orElseGet(() ->
                                roleRepository.save(
                                        new Role(
                                                tenant,
                                                "USER"
                                        )
                                )
                        );

        userRoleRepository.save(
                new UserRole(
                        savedUser,
                        userRole
                )
        );

        return savedUser;
    }
}