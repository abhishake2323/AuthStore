package com.vault.storage.controller;

import com.vault.storage.network.NetworkLogEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private NetworkLogEmitter logEmitter;

    // Simulating file upload handling
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Log generation logic
        String auditMessage = String.format("ACTION=UPLOAD|USER=%s|FILENAME=%s|THREAD=%s",
                username, file.getOriginalFilename(), Thread.currentThread().getName());
        
        logEmitter.sendAuditLog(auditMessage);
        
        return ResponseEntity.ok("File securely stored! Handled by virtual thread: " + Thread.currentThread().isVirtual());
    }
}
