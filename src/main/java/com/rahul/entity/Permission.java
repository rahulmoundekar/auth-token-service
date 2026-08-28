package com.rahul.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_permission_name",
                        columnNames = "name"
                )
        }
)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    protected Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}