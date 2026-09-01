# Auth Token Service Architecture

## High-Level Architecture

```text
                     Client
                       |
                       v
                REST Controllers
                       |
                       v
                Spring Security
                       |
             +---------+---------+
             |                   |
             v                   v
         JWT Filter          RBAC
             |                   |
             +---------+---------+
                       |
                       v
                   Services
                       |
             +---------+---------+
             |                   |
             v                   v
        PostgreSQL           Token Logic
             |
             v
        Row-Level Security
             |
             v
        Tenant Isolation

Login Request
    |
    v
Tenant + Username + Password
    |
    v
AuthenticationService
    |
    +--> User lookup
    +--> Password verification
    +--> Role lookup
    |
    v
JWT Access Token
    +
Refresh Token



Refresh Token
     |
     v
Hash Token
     |
     v
RLS Bootstrap
     |
     v
Resolve Tenant
     |
     v
Set Tenant Context
     |
     v
Validate Refresh Token
     |
     v
Atomic Revocation
     |
     +---- success ---> New Access + Refresh Token
     |
     +---- failure ---> 401


Request
   |
   v
JWT tenant_id
   |
   v
TenantDatabaseContext
   |
   v
PostgreSQL session context
   |
   v
RLS policy
   |
   v
Tenant-scoped rows only


---

# 14.6.4 — Add a Postman collection

For a GitHub portfolio project, this is useful.

Create:

```text
postman/Auth-Token-Service.postman_collection.json