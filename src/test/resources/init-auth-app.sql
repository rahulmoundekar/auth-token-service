CREATE ROLE auth_app
    LOGIN
    PASSWORD 'root';

GRANT CONNECT
    ON DATABASE auth_service
    TO auth_app;

GRANT USAGE
    ON SCHEMA public
    TO auth_app;