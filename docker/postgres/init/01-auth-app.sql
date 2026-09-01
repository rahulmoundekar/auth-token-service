-- =========================================================
-- PostgreSQL bootstrap
-- Runs only when the PostgreSQL data directory is initialized
-- =========================================================

-- Install extension as the PostgreSQL administrator.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create restricted application role.
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

-- Database access.
GRANT CONNECT
    ON DATABASE auth_service
    TO auth_app;

-- Schema access.
GRANT USAGE, CREATE
    ON SCHEMA public
    TO auth_app;