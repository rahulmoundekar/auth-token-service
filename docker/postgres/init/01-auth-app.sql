-- PostgreSQL extensions must be installed by the database administrator.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create the restricted application role.
DO
$$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_roles
            WHERE rolname = 'auth_app'
        ) THEN
            CREATE ROLE auth_app
                LOGIN
                PASSWORD 'root';
        END IF;
    END
$$;

GRANT CONNECT
    ON DATABASE auth_service
    TO auth_app;

GRANT USAGE, CREATE
    ON SCHEMA public
    TO auth_app;