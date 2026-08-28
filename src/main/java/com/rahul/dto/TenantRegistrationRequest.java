package com.rahul.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantRegistrationRequest(

        @NotBlank
        @Size(max = 100)
        String name
) {
}