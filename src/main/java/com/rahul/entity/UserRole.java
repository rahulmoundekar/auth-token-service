package com.rahul.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_role",
                        columnNames = {
                                "user_id",
                                "role_id"
                        }
                )
        }
)
@IdClass(UserRole.UserRoleId.class)
public class UserRole {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;

    protected UserRole() {
    }

    public UserRole(
            User user,
            Role role
    ) {
        this.user = user;
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public static class UserRoleId implements Serializable {

        private java.util.UUID user;
        private java.util.UUID role;

        public UserRoleId() {
        }

        public UserRoleId(
                java.util.UUID user,
                java.util.UUID role
        ) {
            this.user = user;
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (!(o instanceof UserRoleId that)) {
                return false;
            }

            return java.util.Objects.equals(user, that.user)
                    && java.util.Objects.equals(role, that.role);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(
                    user,
                    role
            );
        }
    }
}