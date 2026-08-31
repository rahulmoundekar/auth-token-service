INSERT INTO permissions (name)
VALUES
    ('USER_READ'),
    ('USER_WRITE'),
    ('USER_DELETE'),
    ('ROLE_READ'),
    ('ROLE_WRITE'),
    ('TOKEN_REVOKE')
ON CONFLICT (name) DO NOTHING;