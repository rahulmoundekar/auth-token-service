ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS version BIGINT;

UPDATE refresh_tokens
SET version = 0
WHERE version IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN version SET DEFAULT 0;

ALTER TABLE refresh_tokens
    ALTER COLUMN version SET NOT NULL;

ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMPTZ;