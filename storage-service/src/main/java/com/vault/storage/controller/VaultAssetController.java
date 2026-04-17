package com.vault.storage.controller;

import com.vault.storage.network.UdpTelemetryBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/vault")
public class VaultAssetController {

    private static final Logger log = LoggerFactory.getLogger(VaultAssetController.class);

    @Autowired
    private UdpTelemetryBroker telemetryBroker;

    private boolean isMaliciousSignature(byte[] buffer) {
        // Windows PE (MZ)
        if (buffer[0] == 0x4D && buffer[1] == 0x5A) return true;
        // Shell Script (#!)
        if (buffer[0] == 0x23 && buffer[1] == 0x21) return true;
        // Linux ELF
        if (buffer[0] == 0x7F && buffer[1] == 0x45 && buffer[2] == 0x4C && buffer[3] == 0x46) return true;
        // macOS Mach-O (32-bit and 64-bit variations)
        if ((buffer[0] == (byte) 0xCE && buffer[1] == (byte) 0xFA && buffer[2] == (byte) 0xED && buffer[3] == (byte) 0xFE) ||
            (buffer[0] == (byte) 0xFE && buffer[1] == (byte) 0xED && buffer[2] == (byte) 0xFA && buffer[3] == (byte) 0xCE) ||
            (buffer[0] == (byte) 0xCA && buffer[1] == (byte) 0xFE && buffer[2] == (byte) 0xBA && buffer[3] == (byte) 0xBE)) {
            return true;
        }
        return false;
    }

    @PostMapping("/secure-ingest")
    public ResponseEntity<String> secureIngest(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Trace-Id", defaultValue = "NO_TRACE") String traceId) {
        
        String username = "UNKNOWN";
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                username = SecurityContextHolder.getContext().getAuthentication().getName();
            }
            
            Path storageDirectory = Paths.get("vault-data").toAbsolutePath().normalize();
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }

            Path tempFile = Files.createTempFile(storageDirectory, "upload_", ".tmp");
            String sha256;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (java.io.InputStream inputStream = file.getInputStream();
                 java.security.DigestInputStream digestInputStream = new java.security.DigestInputStream(inputStream, digest);
                 java.io.OutputStream outStream = Files.newOutputStream(tempFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                boolean isFirstBlock = true;

                while ((bytesRead = digestInputStream.read(buffer)) != -1) {
                    if (isFirstBlock && bytesRead >= 4) {
                        if (isMaliciousSignature(buffer)) {
                            throw new SecurityException("MALWARE_DETECTED");
                        }
                        isFirstBlock = false;
                    }
                    outStream.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                Files.deleteIfExists(tempFile);
                throw e; // rethrow exception so it is caught below
            }

            sha256 = HexFormat.of().formatHex(digest.digest());

            Path destination = storageDirectory.resolve(sha256).normalize();
            Files.move(tempFile, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Appends the Gateway Trace ID cleanly into the IoT UDP flow
            String auditMessage = String.format("SYS_EVENT=SECURE_INGEST|TRACE=%s|SUBJECT=%s|ASSET=%s|SHA256=%s|PROC_THREAD=%s",
                    traceId, username, file.getOriginalFilename(), sha256, Thread.currentThread().getName());
            
            telemetryBroker.sendAuditLog(auditMessage);
            
            return ResponseEntity.ok("Asset cryptographically secured. Checksum: " + sha256);
            
        } catch (SecurityException se) {
            String auditMessage = String.format("SYS_EVENT=MALWARE_BLOCKED|TRACE=%s|SUBJECT=%s|ASSET=%s|PROC_THREAD=%s",
                    traceId, username, file.getOriginalFilename(), Thread.currentThread().getName());
            telemetryBroker.sendAuditLog(auditMessage);
            log.warn("Blocked malicious upload attempt from user: {}", username);
            return ResponseEntity.status(403).body("UPLOAD REJECTED: Malicious executable binary signature detected in byte stream.");
        } catch (Exception e) {
            log.error("Internal server error during asset ingestion", e);
            return ResponseEntity.internalServerError().body("Failed to process asset.");
        }
    }

    @GetMapping("/retrieve/{checksum}")
    public ResponseEntity<Resource> retrieveData(@PathVariable String checksum) {
        try {
            Path baseDir = Paths.get("vault-data").toAbsolutePath().normalize();
            Path file = baseDir.resolve(checksum).normalize();
            
            if (!file.startsWith(baseDir)) {
                throw new SecurityException("Path traversal attempt detected.");
            }

            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(file.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + checksum + "\"")
                    .body(resource);
        } catch (SecurityException se) {
            String auditMessage = String.format("SYS_EVENT=PATH_TRAVERSAL_BLOCKED|TARGET=%s|PROC_THREAD=%s",
                    checksum, Thread.currentThread().getName());
            telemetryBroker.sendAuditLog(auditMessage);
            log.warn("Blocked path traversal attempt for target: {}", checksum);
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Internal server error during asset retrieval", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
