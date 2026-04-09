package com.vault.auth.controller;

import com.vault.auth.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        // Dummy authentication for Zero Trust demonstration
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // Real application would check against DB
        if ("admin".equals(username) && "admin123".equals(password)) {
            String jwt = jwtProvider.generateToken(username);
            return ResponseEntity.ok(Map.of("token", jwt));
        }

        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
