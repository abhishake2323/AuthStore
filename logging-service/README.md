# Logging Service

The Logging Service acts as a central telemetry receptor. It captures real-time audit logs transmitted over UDP, verifying cryptographic authenticity to prevent replay attacks and tampering.

## Features
- **Temporal UDP Validation:** Enforces a 5000ms TTL epoch window to block UDP packet spoofing and replay attacks.
- **HMAC Signature Checks:** Validates incoming datagrams from the `storage-service` using shared secrets.
- **Machine Learning Forwarding:** Simulates passing validated logs to a HiveMQ/MQTT Machine Learning Intrusion System.

## Execution
Run this service using:
```bash
mvn spring-boot:run -pl logging-service
```
Listens perpetually on UDP port `9000`. Requires `JWT_SECRET` and `VAULT_KEYSTORE_PASSWORD` environment variables.
