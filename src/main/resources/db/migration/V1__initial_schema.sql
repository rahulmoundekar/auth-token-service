CREATE TABLE tenants
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_tenant_name
        UNIQUE (name)
);

CREATE TABLE users
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_tenant_username
        UNIQUE (tenant_id, username),

    CONSTRAINT fk_user_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants (id)
);

CREATE TABLE roles
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID         NOT NULL,
    name      VARCHAR(100) NOT NULL,

    CONSTRAINT uk_role_tenant_name
        UNIQUE (tenant_id, name),

    CONSTRAINT fk_role_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants (id)
);

CREATE TABLE permissions
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,

    CONSTRAINT uk_permission_name
        UNIQUE (name)
);

CREATE TABLE user_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT uk_user_role
        UNIQUE (user_id, role_id),

    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE,

    PRIMARY KEY (
                 user_id,
                 role_id
        )
);

CREATE TABLE role_permissions
(
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,

    CONSTRAINT uk_role_permission
        UNIQUE (role_id, permission_id),

    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id)
            REFERENCES permissions (id)
            ON DELETE CASCADE,

    PRIMARY KEY (
                 role_id,
                 permission_id
        )
);

CREATE TABLE refresh_tokens
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),

    user_id      UUID        NOT NULL,
    tenant_id    UUID        NOT NULL,

    token_hash   VARCHAR(64) NOT NULL,

    expires_at   TIMESTAMPTZ NOT NULL,

    revoked      BOOLEAN     NOT NULL DEFAULT FALSE,

    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    token_family VARCHAR(36) NOT NULL,

    version      BIGINT      NOT NULL DEFAULT 0,

    revoked_at   TIMESTAMPTZ,

    CONSTRAINT uk_refresh_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_refresh_token_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_token_family
    ON refresh_tokens (token_family);

CREATE INDEX idx_refresh_token_expires_at
    ON refresh_tokens (expires_at);

CREATE INDEX idx_refresh_token_tenant
    ON refresh_tokens (tenant_id);

CREATE INDEX idx_refresh_token_revoked
    ON refresh_tokens (revoked);