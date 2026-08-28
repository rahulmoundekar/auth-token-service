package com.rahul.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_tenant_name",
                        columnNames = {
                                "tenant_id",
                                "name"
                        }
                )
        }
)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_role_tenant"
            )
    )
    private Tenant tenant;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    protected Role() {
    }

    public Role(
            Tenant tenant,
            String name
    ) {
        this.tenant = tenant;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getName() {
        return name;
    }
}