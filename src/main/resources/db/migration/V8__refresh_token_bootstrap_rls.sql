CREATE POLICY refresh_tokens_bootstrap_lookup
    ON refresh_tokens
    FOR SELECT
    USING (
    token_hash =
    current_setting(
            'app.refresh_token_hash',
            true
    )
    );