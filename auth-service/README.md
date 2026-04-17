# Auth Service

The Auth Service manages identity verification, issues JSON Web Tokens (JWT), and securely administrates user accounts for the AuthStore ecosystem.

## Features
- **Stateless Authentication:** Relies entirely on cryptographically signed JWTs, removing the need for server-side sessions.
- **Embedded Database Seeding:** Connects to an embedded H2 database to scaffold an `admin` user on boot.
- **Zero-Trust Token Emission:** Supplies tokens used by downstream services to enforce strict RBAC bounds.

## Execution
Run this service using:
```bash
mvn spring-boot:run -pl auth-service
```
Must launch with required environment variables (`JWT_SECRET`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`).
