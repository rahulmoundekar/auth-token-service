package com.rahul.repository;

import com.rahul.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository
        extends JpaRepository<Permission, UUID> {
}