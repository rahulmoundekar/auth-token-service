package com.rahul.repository;

import com.rahul.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository
        extends JpaRepository<
        RolePermission,
        RolePermission.RolePermissionId> {
}