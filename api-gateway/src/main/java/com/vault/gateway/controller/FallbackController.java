package com.vault.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return buildFallbackResponse("Auth Service", 503);
    }

    @RequestMapping("/storage")
    public ResponseEntity<Map<String, Object>> storageFallback() {
        return buildFallbackResponse("Storage/Vault Service", 503);
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String serviceName, int status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("fault", "NETWORK_CASCADE_PREVENTED");
        response.put("message", serviceName + " is currently unresponsive. The API Gateway Circuit Breaker has safely intercepted the crash.");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
