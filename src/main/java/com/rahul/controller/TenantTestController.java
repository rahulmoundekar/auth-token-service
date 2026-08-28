package com.rahul.controller;

import com.rahul.service.TenantIsolationService;
import com.rahul.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/tenant")
@RequiredArgsConstructor
public class TenantTestController {

    private final TenantIsolationService tenantIsolationService;

    @GetMapping("/users")
    public List<Map<String, Object>> users() {

        TenantContext.requireTenantId();

        return tenantIsolationService.findVisibleUsers();
    }
}