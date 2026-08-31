package com.rahul.controller;

import com.rahul.dto.TenantRegistrationRequest;
import com.rahul.dto.TenantRegistrationResponse;
import com.rahul.dto.UserRegistrationRequest;
import com.rahul.dto.UserRegistrationResponse;
import com.rahul.entity.Tenant;
import com.rahul.entity.User;
import com.rahul.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/tenants")
    @ResponseStatus(HttpStatus.CREATED)
    public TenantRegistrationResponse registerTenant(
            @Valid
            @RequestBody
            TenantRegistrationRequest request
    ) {

        Tenant tenant =
                registrationService.registerTenant(request);

        return new TenantRegistrationResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCreatedAt()
        );
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponse registerUser(
            @Valid
            @RequestBody
            UserRegistrationRequest request
    ) {

        User user =
                registrationService.registerUser(request);

        return new UserRegistrationResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getUsername(),
                user.isEnabled()
        );
    }
}