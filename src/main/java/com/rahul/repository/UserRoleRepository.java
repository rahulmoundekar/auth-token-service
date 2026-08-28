package com.rahul.repository;

import com.rahul.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UserRole.UserRoleId> {
}