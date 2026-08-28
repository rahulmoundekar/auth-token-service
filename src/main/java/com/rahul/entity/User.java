package com.rahul.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_tenant_username",
                        columnNames = {
                                "tenant_id",
                                "username"
                        }
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_user_tenant"
            )
    )
    private Tenant tenant;

    @Column(
            nullable = false,
            length = 100
    )
    private String username;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(
            nullable = false
    )
    private boolean enabled;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected User() {
    }

    public User(
            Tenant tenant,
            String username,
            String passwordHash
    ) {
        this.tenant = tenant;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}