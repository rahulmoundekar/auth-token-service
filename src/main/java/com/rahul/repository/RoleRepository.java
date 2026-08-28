package com.rahul.repository;

import com.rahul.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository
        extends JpaRepository<Role, UUID> {

    boolean existsByTenantIdAndName(
            UUID tenantId,
            String name
    );
}