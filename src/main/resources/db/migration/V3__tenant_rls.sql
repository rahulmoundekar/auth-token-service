ALTER TABLE users
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE users
    FORCE ROW LEVEL SECURITY;

ALTER TABLE roles
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE roles
    FORCE ROW LEVEL SECURITY;

ALTER TABLE refresh_tokens
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE refresh_tokens
    FORCE ROW LEVEL SECURITY;


DROP POLICY IF EXISTS users_tenant_select
    ON users;

CREATE POLICY users_tenant_select
    ON users
    FOR SELECT
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS users_tenant_insert
    ON users;

CREATE POLICY users_tenant_insert
    ON users
    FOR INSERT
    WITH CHECK (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS users_tenant_update
    ON users;

CREATE POLICY users_tenant_update
    ON users
    FOR UPDATE
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    )
    WITH CHECK (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS users_tenant_delete
    ON users;

CREATE POLICY users_tenant_delete
    ON users
    FOR DELETE
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS roles_tenant_select
    ON roles;

CREATE POLICY roles_tenant_select
    ON roles
    FOR SELECT
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS roles_tenant_insert
    ON roles;

CREATE POLICY roles_tenant_insert
    ON roles
    FOR INSERT
    WITH CHECK (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS roles_tenant_update
    ON roles;

CREATE POLICY roles_tenant_update
    ON roles
    FOR UPDATE
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    )
    WITH CHECK (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS roles_tenant_delete
    ON roles;

CREATE POLICY roles_tenant_delete
    ON roles
    FOR DELETE
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS refresh_tokens_tenant_select
    ON refresh_tokens;

CREATE POLICY refresh_tokens_tenant_select
    ON refresh_tokens
    FOR SELECT
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS refresh_tokens_tenant_insert
    ON refresh_tokens;

CREATE POLICY refresh_tokens_tenant_insert
    ON refresh_tokens
    FOR INSERT
    WITH CHECK (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS refresh_tokens_tenant_update
    ON refresh_tokens;

CREATE POLICY refresh_tokens_tenant_update
    ON refresh_tokens
    FOR UPDATE
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    )
    WITH CHECK (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );


DROP POLICY IF EXISTS refresh_tokens_tenant_delete
    ON refresh_tokens;

CREATE POLICY refresh_tokens_tenant_delete
    ON refresh_tokens
    FOR DELETE
    USING (
    tenant_id::text =
    current_setting(
            'app.current_tenant',
            true
    )
    );