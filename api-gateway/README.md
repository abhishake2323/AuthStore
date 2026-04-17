# API Gateway Service

The API Gateway is the central entry point for the AuthStore ecosystem. It is an edge service built with Spring Cloud Gateway that routes and load-balances requests to the downstream microservices (`auth-service` and `storage-service`).

## Features
- **TLS 1.3 Reverse Proxy:** Terminates SSL/TLS connections using self-signed PKCS12 certificates.
- **Trace correlation:** Injects a unique `X-Trace-Id` header into all incoming requests to ensure requests can be tracked across the distributed environment.
- **Circuit Breaking & Resilience:** Intercepts and limits failing routes.

## Execution
Run this service using:
```bash
mvn spring-boot:run -pl api-gateway
```
Ensure that the `JWT_SECRET` environment variable is set.
