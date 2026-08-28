package com.rahul.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(
        name = "role_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_permission",
                        columnNames = {
                                "role_id",
                                "permission_id"
                        }
                )
        }
)
@IdClass(RolePermission.RolePermissionId.class)
public class RolePermission {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "permission_id",
            nullable = false
    )
    private Permission permission;

    protected RolePermission() {
    }

    public RolePermission(
            Role role,
            Permission permission
    ) {
        this.role = role;
        this.permission = permission;
    }

    public Role getRole() {
        return role;
    }

    public Permission getPermission() {
        return permission;
    }

    public static class RolePermissionId implements Serializable {

        private UUID role;
        private UUID permission;

        public RolePermissionId() {
        }

        public RolePermissionId(
                UUID role,
                UUID permission
        ) {
            this.role = role;
            this.permission = permission;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (!(o instanceof RolePermissionId that)) {
                return false;
            }

            return java.util.Objects.equals(
                    role,
                    that.role
            )
                    && java.util.Objects.equals(
                    permission,
                    that.permission
            );
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(
                    role,
                    permission
            );
        }
    }
}