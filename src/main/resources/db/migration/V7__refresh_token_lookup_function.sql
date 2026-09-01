CREATE OR REPLACE FUNCTION find_refresh_token_tenant(
    p_token_hash VARCHAR
)
    RETURNS TABLE (
                      id UUID,
                      user_id UUID,
                      tenant_id UUID,
                      token_hash VARCHAR,
                      expires_at TIMESTAMPTZ,
                      revoked BOOLEAN,
                      revoked_at TIMESTAMPTZ,
                      token_family VARCHAR,
                      version BIGINT,
                      created_at TIMESTAMPTZ
                  )
    LANGUAGE SQL
    SECURITY DEFINER
    SET search_path = public
AS
$$
SELECT
    rt.id,
    rt.user_id,
    u.tenant_id,
    rt.token_hash,
    rt.expires_at,
    rt.revoked,
    rt.revoked_at,
    rt.token_family,
    rt.version,
    rt.created_at
FROM refresh_tokens rt
         JOIN users u
              ON u.id = rt.user_id
WHERE rt.token_hash = p_token_hash
LIMIT 1;
$$;

REVOKE ALL
    ON FUNCTION find_refresh_token_tenant(VARCHAR)
    FROM PUBLIC;

GRANT EXECUTE
    ON FUNCTION find_refresh_token_tenant(VARCHAR)
    TO auth_app;