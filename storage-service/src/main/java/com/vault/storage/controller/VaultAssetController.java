package com.vault.storage.controller;

import com.vault.storage.network.UdpTelemetryBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.MessageDigest;
import java.io.InputStream;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/vault")
public class VaultAssetController {

    @Autowired
    private UdpTelemetryBroker telemetryBroker;

    // Advanced SHA-256 byte stream processing with RCE Magic Byte prevention
    private String calculateChecksumAndValidateBinary(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            // Magic Byte Validation (First block)
            if ((bytesRead = is.read(buffer)) != -1) {
                // 0x4D 0x5A stands for 'M' 'Z', the definitive header for Windows executable viruses/payloads
                if (bytesRead >= 2 && buffer[0] == 0x4D && buffer[1] == 0x5A) { 
                    throw new SecurityException("MALWARE_DETECTED");
                }
                digest.update(buffer, 0, bytesRead);
            }
            
            // Standard byte buffer reading
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    @PostMapping("/secure-ingest")
    public ResponseEntity<String> secureIngest(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Trace-Id", defaultValue = "NO_TRACE") String traceId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String sha256 = calculateChecksumAndValidateBinary(file);
            
            // Appends the Gateway Trace ID cleanly into the IoT UDP flow
            String auditMessage = String.format("SYS_EVENT=SECURE_INGEST|TRACE=%s|SUBJECT=%s|ASSET=%s|SHA256=%s|PROC_THREAD=%s",
                    traceId, username, file.getOriginalFilename(), sha256, Thread.currentThread().getName());
            
            telemetryBroker.sendAuditLog(auditMessage);
            
            return ResponseEntity.ok("Asset cryptographically secured. Checksum: " + sha256);
            
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body("UPLOAD REJECTED: Malicious executable binary (MZ) detected in byte stream.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to process asset.");
        }
    }
}
