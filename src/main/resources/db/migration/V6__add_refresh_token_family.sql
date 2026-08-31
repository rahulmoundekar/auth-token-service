ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS token_family VARCHAR(36);

UPDATE refresh_tokens
SET token_family = id::text
WHERE token_family IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN token_family SET NOT NULL;