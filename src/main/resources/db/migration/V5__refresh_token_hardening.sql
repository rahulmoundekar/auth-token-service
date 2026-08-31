ALTER TABLE refresh_tokens
    ALTER COLUMN version SET DEFAULT 0;

ALTER TABLE refresh_tokens
    ALTER COLUMN revoked SET DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_refresh_token_user_revoked
    ON refresh_tokens(user_id, revoked);

CREATE INDEX IF NOT EXISTS idx_refresh_token_family_revoked
    ON refresh_tokens(token_family, revoked);