# Auth Token Service

A production-oriented, multi-tenant authentication and authorization service built with **Java 21, Spring Boot 4.1.1, Spring Security, JWT, PostgreSQL 17, Flyway, PostgreSQL Row-Level Security (RLS), Docker, Testcontainers, and Maven**.

The service is designed to provide a reusable authentication boundary for multiple applications while keeping tenant data isolated at both the application-security layer and the PostgreSQL database layer.

---

## 1. Purpose

The purpose of this service is to centralize authentication and authorization responsibilities instead of implementing login, JWT generation, refresh-token handling, role checks, and tenant isolation independently in every business microservice.

The service provides:

- Tenant registration
- User registration inside a tenant
- BCrypt password hashing
- Username uniqueness per tenant
- JWT access-token issuance
- Role-based authorization (USER / ADMIN)
- Rotating refresh tokens
- Refresh-token reuse detection
- Token-family revocation
- Logout
- Concurrent refresh protection using optimistic concurrency/versioning
- Tenant context management
- PostgreSQL Row-Level Security (RLS)
- Flyway-managed database schema
- Standardized API error responses
- OpenAPI / Swagger documentation
- Actuator health/readiness endpoints
- Docker Compose local deployment
- Testcontainers integration testing

---

## 2. Where Can This Service Be Used?

This service can act as an authentication service for:

- SaaS applications with multiple tenants/customers
- B2B platforms
- Internal enterprise applications
- Admin portals
- Microservice ecosystems
- Multi-organization REST APIs
- Applications where authorization must be enforced consistently across services

A typical architecture is:

```text
                    +----------------------+
                    |      Web / Mobile    |
                    |       Clients        |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |  API Gateway / App   |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |  Auth Token Service  |
                    |                      |
                    | JWT + RBAC           |
                    | Refresh Tokens       |
                    | Tenant Context       |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |    PostgreSQL 17     |
                    |                      |
                    | RLS Tenant Isolation |
                    +----------------------+
```

Business microservices can trust a validated JWT and use the tenant identity/roles supplied by the authentication layer without owning password storage or refresh-token lifecycle logic.

---

## 3. Technology Stack

| Technology | Version / Role |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Security | JWT authentication / RBAC |
| Spring Data JPA | Persistence |
| PostgreSQL | 17 |
| Flyway | Database migration/versioning |
| JJWT | 0.13.0 |
| Docker / Docker Compose | Local containerized runtime |
| Testcontainers | PostgreSQL integration testing |
| Maven | Build / test / package |
| Lombok | Boilerplate reduction |
| SpringDoc OpenAPI | Swagger / OpenAPI |
| Spring Boot Actuator | Health / readiness / observability |

---

## 4. Core Architecture

```text
HTTP Request
     |
     v
Spring Security Filter Chain
     |
     +--------------------+
     |                    |
     v                    v
JWT Authentication      Public Auth APIs
     |
     v
Authorization / RBAC
     |
     v
Tenant Context
     |
     v
Service Layer
     |
     +----------------------+
     |                      |
     v                      v
JPA / JDBC             Refresh Token Logic
     |                      |
     +----------+-----------+
                |
                v
           PostgreSQL
                |
                v
          PostgreSQL RLS
```

### Security layers

1. HTTP/API validation
2. Spring Security authentication
3. JWT signature and claim validation
4. Role-based authorization
5. Tenant context
6. PostgreSQL Row-Level Security
7. Restricted application database role

This provides defense in depth: even if application code accidentally issues a broad query, PostgreSQL RLS is an additional data-isolation boundary.

---

## 5. Tenant Model

A tenant represents an isolated customer/organization.

Example tenants:

```text
Tenant A = 11111111-1111-1111-1111-111111111111
Tenant B = 22222222-2222-2222-2222-222222222222
```

A username is unique within a tenant, not globally:

```text
Tenant A + alice   -> allowed
Tenant B + alice   -> allowed
Tenant A + alice   -> duplicate
```

The JWT contains the authenticated tenant identity.

Application requests can also use the `X-Tenant-Id` header where applicable, but the application must not allow that header to override the tenant identity contained in a validated JWT.

---

## 6. Database Design

The main tables are:

```text
+-------------------+
| tenants           |
+---------+---------+
          |
          | 1:N
          v
+-------------------+
| users             |
+---------+---------+
          |
          | N:M
          v
+-------------------+       +-------------------+
| user_roles        +------>+ roles             |
+-------------------+       +---------+---------+
                                      |
                                      | N:M
                                      v
                              +-------------------+
                              | role_permissions  |
                              +---------+---------+
                                        |
                                        v
                              +-------------------+
                              | permissions       |
                              +-------------------+

+-------------------+
| refresh_tokens    |
+-------------------+
```

### `users`

Stores tenant-owned user accounts and password hashes.

Important constraint:

```text
UNIQUE (tenant_id, username)
```

### `roles`

Stores tenant-scoped roles such as `USER` and `ADMIN`.

### `permissions`

Stores application permission definitions.

### `user_roles`

Maps users to roles.

### `role_permissions`

Maps roles to permissions.

### `refresh_tokens`

Stores only refresh-token hashes, never the raw refresh token.

Important security fields include:

```text
token_hash
expires_at
revoked
revoked_at
token_family
version
tenant_id
```

The `version` field participates in optimistic concurrency control.

---

## 7. PostgreSQL Row-Level Security

RLS is enabled and forced for the tenant-sensitive tables.

The current design protects:

```text
users
roles
refresh_tokens
```

Conceptually:

```text
Application tenant context
        |
        v
app.current_tenant
        |
        v
PostgreSQL RLS policy
        |
        v
Only rows belonging to that tenant
```

The application database role is intentionally restricted and must not bypass RLS.

Expected role properties:

```text
rolsuper     = false
rolbypassrls = false
```

---

## 8. Database User Model

Development uses two logical database identities:

```text
postgres
  -> database administration / container initialization

 auth_app
  -> Spring Boot application connection
```

The application connects as `auth_app`.

Do not grant `SUPERUSER` or `BYPASSRLS` to `auth_app`.

---

## 9. Flyway Database Migrations

Database schema evolution is controlled by Flyway.

Hibernate does **not** own schema changes.

The application uses:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

So the lifecycle is:

```text
Flyway migration
      |
      v
PostgreSQL schema
      |
      v
Hibernate validates schema
```

The project currently validates/applies the complete migration chain in the integration environment, with the latest test run validating **8 migrations**.

Migration responsibilities include:

```text
V1  Initial schema / extensions / tables
V2  Seed permission data
V3  Tenant RLS policies
V4+ Refresh-token hardening / compatibility migrations
V7  Refresh-token lookup security migration (historical)
V8  Refresh-token bootstrap RLS
```

> Applied Flyway migrations should be treated as immutable. New schema changes should be added as new migrations rather than editing old migrations.

### Check migration history

```powershell
docker compose exec postgres psql -U postgres -d auth_service -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

Every applied migration should have:

```text
success = t
```

---

## 10. Manual Database Setup vs Flyway

### Manual setup

For learning/debugging, PostgreSQL can be inspected manually using `psql`:

```powershell
docker compose exec postgres psql -U postgres -d auth_service
```

Useful checks:

```sql
SELECT current_database();

SELECT rolname, rolsuper, rolbypassrls
FROM pg_roles
WHERE rolname = 'auth_app';

SELECT relname, relrowsecurity, relforcerowsecurity
FROM pg_class
WHERE relname IN ('users', 'roles', 'refresh_tokens');
```

### Production approach

Do not rely on manually created tables/policies.

Use:

```text
Flyway → schema + indexes + constraints + RLS migrations
```

Manual SQL should be reserved for administration/diagnostics and emergency remediation.

---

## 11. Authentication Flow

### Registration

```text
Register request
      |
      v
Validate tenant
      |
      v
Validate username uniqueness
      |
      v
BCrypt password hashing
      |
      v
Create user
      |
      v
Assign USER role
```

### Login

```text
Tenant + username + password
             |
             v
       Load tenant user
             |
             v
       Verify BCrypt hash
             |
             v
       Load roles
             |
             v
      Generate JWT access token
             |
             +----> Generate refresh token
             |
             v
           Response
```

### Refresh

```text
Raw refresh token
      |
      v
Hash token
      |
      v
RLS bootstrap / token lookup
      |
      v
Resolve tenant
      |
      v
Set tenant context
      |
      v
Validate token
      |
      v
Atomically revoke current token
      |
      v
Issue new access + refresh token
```

### Reuse detection

```text
R1
 |
 +---- refresh ----> R1 revoked + R2 issued
 |
 +---- reuse ------> 401 Unauthorized
```

### Logout

```text
Refresh token
      |
      v
Resolve tenant + token family
      |
      v
Revoke token family
      |
      v
204 No Content
```

---

## 12. Refresh-Token Concurrency Protection

Refresh-token rotation is protected using optimistic concurrency/versioning.

Two requests using the same token must not both create successful replacements.

Expected behavior:

```text
                 R1
              /      \
             /        \
       Request A    Request B
           |             |
           v             v
      update version  update version
           |             |
          1 row         0 rows
           |             |
          200           401
```

This protects against refresh races caused by concurrent client requests, duplicated messages, retries, or multiple application instances.

---

## 13. REST API

Base URL:

```text
http://localhost:8082
```

### Tenant registration

```http
POST /api/auth/tenants
```

Request:

```json
{
  "name": "Acme Corp"
}
```

Response:

```http
201 Created
```

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "name": "Acme Corp",
  "createdAt": "2026-09-01T12:00:00Z"
}
```

### User registration

```http
POST /api/auth/register
```

Request:

```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "username": "alice",
  "password": "Password@123"
}
```

Response:

```http
201 Created
```

```json
{
  "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "username": "alice",
  "enabled": true
}
```

The password is never returned.

### Login

```http
POST /api/auth/login
```

Request:

```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "username": "alice",
  "password": "Password@123"
}
```

Response:

```http
200 OK
```

```json
{
  "accessToken": "<JWT>",
  "refreshToken": "<REFRESH_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Refresh

```http
POST /api/auth/refresh
```

Request:

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

Response:

```http
200 OK
```

```json
{
  "accessToken": "<NEW_JWT>",
  "refreshToken": "<NEW_REFRESH_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Logout

```http
POST /api/auth/logout
```

Request:

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

Response:

```http
204 No Content
```

### User APIs

```http
GET /api/user/profile
GET /api/user/tenant
```

Requires a valid JWT.

### Admin API

```http
GET /api/admin/dashboard
```

Requires a valid JWT with `ADMIN` role.

### Tenant diagnostic/test APIs

```http
GET /api/test/tenant/users
GET /api/test/tenant/me
```

These endpoints are intended for validation/testing of authentication and tenant context behavior.

---

## 14. Authentication Header

Protected requests use:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

Tenant-aware requests may also include:

```http
X-Tenant-Id: <TENANT_UUID>
```

The JWT remains the authoritative authenticated tenant identity. The service must not allow a caller-controlled tenant header to override a validated JWT tenant.

---

## 15. HTTP Status Contract

| Scenario | Status |
|---|---:|
| Tenant/User registration | 201 |
| Successful login | 200 |
| Successful refresh | 200 |
| Successful logout | 204 |
| Validation error | 400 |
| Authentication failure | 401 |
| Invalid/expired/reused refresh token | 401 |
| Missing/invalid JWT | 401 |
| Insufficient role | 403 |
| Unexpected server error | 500 |

---

## 16. Standard Error Response

The service exposes a consistent error model.

Example:

```json
{
  "timestamp": "2026-09-01T12:00:00Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Authentication is required",
  "path": "/api/user/profile"
}
```

Validation responses identify the API request problem without exposing secrets, SQL details, or stack traces.

Security-related errors should avoid revealing whether a username, password, token or tenant specifically exists.

---

## 17. Swagger / OpenAPI

Once the application is running:

```text
Swagger UI
http://localhost:8082/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8082/v3/api-docs
```

Swagger is intended primarily for local/test usage. In a production deployment it should be disabled or protected unless explicitly required.

---

## 18. Health / Readiness / Liveness

Actuator endpoints:

```text
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
GET /actuator/info
```

Example:

```json
{
  "status": "UP"
}
```

Only safe Actuator endpoints should be exposed publicly.

---

## 19. Configuration

The project uses environment-driven configuration.

Key variables:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
SERVER_PORT

JWT_SECRET
JWT_ISSUER
JWT_ACCESS_TOKEN_EXPIRATION

REFRESH_TOKEN_EXPIRATION
JAVA_TOOL_OPTIONS
SPRING_PROFILES_ACTIVE
```

### Example

Copy:

```text
.env.example
```

to a local `.env` file and replace placeholders.

Never commit `.env` or production secrets.

### JWT secret

The JWT HMAC key must be at least 256 bits for HS256.

For production, generate a strong random secret and supply it through the deployment environment or secret manager.

### UTC

The project standardizes time handling on UTC:

```text
JAVA_TOOL_OPTIONS=-Duser.timezone=UTC
```

---

## 20. Configuration Ownership

```text
application.yaml
     |
     +--> common defaults
     |
     +--> environment variables

Flyway
     |
     +--> database schema

Hibernate
     |
     +--> validate schema
```

Do not use `ddl-auto: update` for the production configuration.

---

## 21. Local Development - Manual Execution

### Step 1 - Start PostgreSQL

Make sure Docker Desktop is running.

```powershell
docker compose up -d postgres
```

Check:

```powershell
docker compose ps
```

The database should become healthy.

### Step 2 - Configure environment

PowerShell example:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5433"
$env:DB_NAME="auth_service"
$env:DB_USERNAME="auth_app"
$env:DB_PASSWORD="<LOCAL_DB_PASSWORD>"

$env:JWT_SECRET="<LOCAL_256_BIT_OR_LONGER_SECRET>"
$env:JWT_ISSUER="auth-token-service"
$env:JWT_ACCESS_TOKEN_EXPIRATION="900000"
$env:REFRESH_TOKEN_EXPIRATION="604800000"

$env:JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"
```

### Step 3 - Start Spring Boot

```powershell
mvn spring-boot:run
```

or:

```powershell
./mvnw spring-boot:run
```

### Step 4 - Verify

```powershell
Invoke-RestMethod http://localhost:8082/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

---

## 22. Run With Docker Compose

Build and start:

```powershell
docker compose up -d --build
```

Check:

```powershell
docker compose ps
```

View application logs:

```powershell
docker compose logs -f auth-token-service
```

Stop:

```powershell
docker compose down
```

Remove the database volume too:

```powershell
docker compose down -v
```

> `docker compose down -v` deletes local PostgreSQL data.

### Docker networking

When Spring Boot runs inside Docker:

```text
DB_HOST=postgres
DB_PORT=5432
```

When Spring Boot runs directly on Windows:

```text
DB_HOST=localhost
DB_PORT=5433
```

---

## 23. Docker Architecture

```text
Windows Host
    |
    +---------------------------+
    |                           |
    v                           v
PostgreSQL container      Auth application
postgres:17               Spring Boot/JRE
    |                           |
    +-------- Docker network ---+
                |
                v
             postgres:5432
```

The application runtime container should run as a non-root user.

---

## 24. Testing Strategy

The project has multiple testing layers.

### Unit/application context tests

Basic Spring application context verification.

### Integration tests

Testcontainers provisions a real PostgreSQL 17 container for integration testing.

### Database schema tests

Verify:

- required tables
- migration behavior
- RLS enabled
- RLS forced
- required policies

### Security regression tests

Verify:

- unauthenticated access → 401
- invalid JWT → 401
- USER → ADMIN → 403
- ADMIN → ADMIN → success
- correct tenant claim
- tenant header cannot override JWT tenant
- refresh rotation
- refresh-token reuse rejection
- logout
- token-family revocation
- concurrent refresh protection

### Final verified test run

```text
Tests run: 29
Failures: 0
Errors: 0
```

This was the completed project security/integration baseline before final documentation packaging.

---

## 25. Running the Full Test Suite

```powershell
mvn clean test
```

or:

```powershell
./mvnw clean test
```

A successful build ends with:

```text
BUILD SUCCESS
```

Then package the service:

```powershell
mvn clean package
```

---

## 26. Important Testcontainers Detail

Testcontainers intentionally uses dynamic host ports.

Example:

```text
localhost:58811
localhost:58825
localhost:61407
```

Do not hard-code these ports.

Integration tests use `@DynamicPropertySource` to point Spring's datasource to the current Testcontainers JDBC URL.

This keeps local Docker Compose (`localhost:5433`) separate from integration-test infrastructure.

---

## 27. Security Hardening Completed

The project was progressively hardened through the implementation steps.

```text
01  Project setup and architecture                       ✅
02  PostgreSQL persistence                               ✅
03  Tenant model                                         ✅
04  User registration                                    ✅
05  BCrypt password hashing                              ✅
06  Login + JWT access token                             ✅
07  JWT validation                                       ✅
08  Role-based authorization                             ✅
09  Tenant context                                       ✅
10  Refresh-token issuance                               ✅
11  Refresh-token persistence                            ✅
12  Logout / revocation                                  ✅
13  PostgreSQL RLS                                       ✅
13.5 Database integration tests                          ✅
13.6 Tenant isolation tests                              ✅
13.7 HTTP security regression tests                      ✅
13.8 Refresh rotation / reuse / logout hardening         ✅
13.9 Concurrent refresh protection                       ✅
13.10 Final security/configuration cleanup               ✅
14.1 API contract cleanup                                ✅
14.2 Standard API errors                                 ✅
14.3 Global exception handling                           ✅
14.4 Validation / HTTP status consistency                ✅
14.5 OpenAPI / Swagger                                   ✅
14.6 API documentation package                           ✅
14.7 Environment/configuration profiles                  ✅
14.8 Docker production readiness                         ✅
14.9 Health/readiness/observability                      ✅
14.10 Final security configuration review                ✅
14.11 Final README / GitHub release package              ✅
```

---

## 28. Security Properties

### Passwords

Passwords are BCrypt-hashed and are never returned by the API.

### Access tokens

JWT access tokens are signed using an HMAC key meeting the required minimum key strength for HS256.

### Refresh tokens

Raw refresh tokens are not stored in PostgreSQL. Only token hashes are persisted.

### Rotation

A successful refresh rotates the refresh token.

### Reuse detection

A previously revoked refresh token is rejected.

### Token family

Tokens are linked to a token family to support family-level revocation.

### Concurrency

Refresh-token updates use versioning/optimistic concurrency to prevent two simultaneous refreshes from both succeeding.

### Tenant isolation

Tenant isolation is enforced in PostgreSQL using RLS.

### Database role

The application role is not a superuser and must not bypass RLS.

### Error leakage

API errors should not reveal database SQL, JWT secrets, passwords, hashes, or stack traces.

---

## 29. Operational Logging

Logs should contain useful operational identifiers such as:

```text
correlationId
request endpoint
business outcome
tenantId where appropriate
```

Logs must not contain:

```text
password
password_hash
access token
refresh token
token_hash
JWT_SECRET
DB_PASSWORD
```

A client can supply:

```http
X-Correlation-Id: <request-id>
```

and the service can return the same value in the response header.

---

## 30. Manual Verification Commands

### Check application health

```powershell
Invoke-RestMethod http://localhost:8082/actuator/health
```

### Check Swagger

```text
http://localhost:8082/swagger-ui.html
```

### Check OpenAPI

```text
http://localhost:8082/v3/api-docs
```

### Check database role

```powershell
docker compose exec postgres psql -U postgres -d auth_service -c "SELECT rolname, rolsuper, rolbypassrls FROM pg_roles WHERE rolname = 'auth_app';"
```

### Check RLS

```powershell
docker compose exec postgres psql -U postgres -d auth_service -c "SELECT relname, relrowsecurity, relforcerowsecurity FROM pg_class WHERE relname IN ('users', 'roles', 'refresh_tokens');"
```

### Check Flyway

```powershell
docker compose exec postgres psql -U postgres -d auth_service -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

---

## 31. Postman

A Postman collection is included in:

```text
postman/Auth-Token-Service.postman_collection.json
```

Recommended execution order:

```text
1. Register Tenant
2. Register User
3. Login
4. Call User API
5. Call Admin API with an ADMIN user
6. Refresh token
7. Reuse old refresh token (should fail)
8. Logout
9. Refresh after logout (should fail)
```

Do not commit real access/refresh tokens in the collection.

---

## 32. Project Structure

```text
src/main/java/com/rahul/
├── config/
│   ├── JwtProperties.java
│   ├── PasswordConfig.java
│   └── RefreshTokenProperties.java
│
├── controller/
│   ├── AuthController.java
│   ├── RegistrationController.java
│   ├── UserController.java
│   ├── AdminController.java
│   └── TenantTestController.java
│
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RefreshTokenRequest.java
│   ├── RefreshTokenResponse.java
│   ├── LogoutRequest.java
│   ├── TenantRegistrationRequest.java
│   ├── TenantRegistrationResponse.java
│   ├── UserRegistrationRequest.java
│   ├── UserRegistrationResponse.java
│   └── ApiErrorResponse.java
│
├── entity/
│   ├── Tenant.java
│   ├── User.java
│   ├── Role.java
│   ├── Permission.java
│   ├── UserRole.java
│   ├── RolePermission.java
│   └── RefreshToken.java
│
├── exception/
│   ├── AuthenticationException.java
│   ├── InvalidRefreshTokenException.java
│   ├── RefreshTokenReuseException.java
│   └── GlobalExceptionHandler.java
│
├── repository/
│   └── ...
│
├── security/
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── RefreshTokenGenerator.java
│   ├── TenantContext.java
│   ├── SecurityConfig.java
│   ├── RestAuthenticationEntryPoint.java
│   └── RestAccessDeniedHandler.java
│
└── service/
    ├── AuthenticationService.java
    ├── RegistrationService.java
    ├── RefreshTokenService.java
    ├── TenantIsolationService.java
    └── UserIdentityService.java

src/main/resources/
├── application.yaml
└── db/migration/
    └── Flyway SQL migrations

src/test/java/
└── integration/security/schema tests

docker/
└── postgres initialization scripts

postman/
└── Auth-Token-Service.postman_collection.json

docs/
├── API.md
├── ARCHITECTURE.md
└── CONFIGURATION.md
```

---

## 33. Common Problems / Troubleshooting

### `WeakKeyException`

Make sure `JWT_SECRET` is at least 32 bytes and that the same property is being used by `JwtService`.

### PostgreSQL password authentication failed

Verify:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

and confirm the role exists:

```sql
SELECT rolname FROM pg_roles WHERE rolname = 'auth_app';
```

### `Asia/Calcutta` timezone error

Use UTC:

```text
JAVA_TOOL_OPTIONS=-Duser.timezone=UTC
```

### `missing table` during Hibernate validation

Check Flyway first:

```text
flyway_schema_history
```

Do not switch Hibernate to `ddl-auto: update` as a workaround.

### Testcontainers stale/closed port

Do not hard-code a dynamic Testcontainers port.

Use the current container JDBC URL through `@DynamicPropertySource` and keep test context/container lifecycle consistent.

### Concurrent refresh returns 500

The losing request must translate the optimistic-concurrency conflict into the service's normal invalid-refresh-token response rather than leaking a persistence exception.

---

## 34. Production Deployment Principles

For production:

- supply secrets through a secret manager or deployment environment
- use a strong randomly generated JWT signing secret
- use TLS/HTTPS
- disable or protect Swagger if not needed
- expose only safe Actuator endpoints
- use `ddl-auto=validate`
- run Flyway migrations as part of deployment
- use a restricted application database user
- keep PostgreSQL RLS enabled and forced
- use separate database credentials for application and administration
- never log credentials or tokens
- monitor readiness and liveness
- preserve refresh-token rotation and concurrency controls

---

## 35. Final Project Execution

### Option A - Local Java + Docker PostgreSQL

```powershell
# Start PostgreSQL
docker compose up -d postgres

# Set required local environment variables
$env:DB_HOST="localhost"
$env:DB_PORT="5433"
$env:DB_NAME="auth_service"
$env:DB_USERNAME="auth_app"
$env:DB_PASSWORD="<LOCAL_DB_PASSWORD>"
$env:JWT_SECRET="<STRONG_LOCAL_SECRET>"
$env:JWT_ISSUER="auth-token-service"
$env:JWT_ACCESS_TOKEN_EXPIRATION="900000"
$env:REFRESH_TOKEN_EXPIRATION="604800000"
$env:JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"

# Run application
mvn spring-boot:run
```

Then verify:

```text
http://localhost:8082/actuator/health
http://localhost:8082/swagger-ui.html
```

### Option B - Full Docker

```powershell
docker compose up -d --build
docker compose ps
docker compose logs -f auth-token-service
```

Verify:

```text
http://localhost:8082/actuator/health
http://localhost:8082/swagger-ui.html
```

### Option C - Run all tests

```powershell
mvn clean test
```

Expected final baseline:

```text
Tests run: 29
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### Option D - Create the application artifact

```powershell
mvn clean package
```

The generated executable JAR is placed under:

```text
target/
```

---

## 36. GitHub Release Checklist

Before pushing the final repository:

```text
[ ] README.md
[ ] docs/API.md
[ ] docs/ARCHITECTURE.md
[ ] docs/CONFIGURATION.md
[ ] postman/Auth-Token-Service.postman_collection.json
[ ] .env.example
[ ] .gitignore
[ ] Dockerfile
[ ] docker-compose.yml
[ ] Flyway migrations
[ ] integration/security tests
[ ] mvn clean test passes
[ ] mvn clean package passes
[ ] no .env committed
[ ] no real JWT secret committed
[ ] no real passwords committed
[ ] no tokens committed
```

Recommended Git commands:

```powershell
git status
git add .
git commit -m "feat: production-ready auth token service"
git tag -a v1.0.0 -m "Auth Token Service 1.0.0"
git push origin main
git push origin v1.0.0
```

---

## 37. Interview / Resume Highlights

This project demonstrates practical experience with:

- Spring Boot REST API development
- Spring Security
- JWT authentication and authorization
- BCrypt password hashing
- Role-based access control
- Multi-tenant architecture
- PostgreSQL Row-Level Security
- Secure refresh-token rotation
- Refresh-token reuse detection
- Token-family revocation
- Optimistic concurrency control
- Database migrations with Flyway
- Docker and Docker Compose
- Testcontainers
- Integration testing
- API validation and standard error handling
- OpenAPI / Swagger
- Health/readiness endpoints
- Production configuration and secret management

### Example resume statement

> Designed and implemented a production-oriented multi-tenant authentication service using Spring Boot, Spring Security, JWT, PostgreSQL RLS and Flyway, including rotating refresh tokens, reuse detection, token-family revocation, RBAC, tenant isolation and concurrent refresh protection, with Testcontainers-based integration/security regression testing.

---

## 38. Final Status

The project has completed the implementation and hardening work covered by Steps 1 through 14.11.

Final verified security/integration baseline:

```text
29 tests
0 failures
0 errors
```

The service is suitable as a portfolio project and as a reusable authentication foundation for multi-tenant Spring-based applications.

---

## License

Add your preferred license before public release, for example MIT.
