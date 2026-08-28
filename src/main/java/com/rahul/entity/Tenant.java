package com.rahul.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "tenants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tenant_name",
                        columnNames = "name"
                )
        }
)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String name;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected Tenant() {
    }

    public Tenant(String name) {
        this.name = name;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}