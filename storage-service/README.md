# Storage Service

The Storage Service handles the ingestion, validation, cryptographic hashing, and persistency of binary assets uploaded by authenticated users.

## Features
- **Secure File Ingestion:** Aggressively filters incoming byte streams via a comprehensive Magic Byte detection matrix, explicitly blocking Windows (MZ), Linux (ELF), macOS (Mach-O), and script-based cross-platform execution payloads, while enforcing strict Path Traversal guardrails.
- **Stream Hashing:** Employs `DigestInputStream` memory optimization and streaming techniques to process files to disk while avoiding Out-of-Memory crashes.
- **IoT UDP Telemetry Emission:** Audits incoming files and transmits cryptographic HMAC telemetry via UDP to the `logging-service`.
- **Multipart Restrictions:** Strictly bounds upload sizes to 50MB to suppress DoS attack vectors.

## Execution
Run this service using:
```bash
mvn spring-boot:run -pl storage-service
```
This service communicates with `logging-service` via UDP. Requires `JWT_SECRET` and `UDP_LOG_HOST` environment variables.
