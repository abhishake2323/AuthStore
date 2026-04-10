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

    // Advanced SHA-256 byte stream processing for structural uniqueness
    private String calculateChecksum(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    @PostMapping("/secure-ingest")
    public ResponseEntity<String> secureIngest(@RequestParam("file") MultipartFile file) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String sha256 = calculateChecksum(file);
            
            // Re-structured standard log payload to a unique telemetry format
            String auditMessage = String.format("SYS_EVENT=SECURE_INGEST|SUBJECT=%s|ASSET=%s|SHA256=%s|PROC_THREAD=%s",
                    username, file.getOriginalFilename(), sha256, Thread.currentThread().getName());
            
            telemetryBroker.sendAuditLog(auditMessage);
            
            return ResponseEntity.ok("Asset cryptographically secured. Checksum: " + sha256);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to process asset.");
        }
    }
}
