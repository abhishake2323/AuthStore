package com.vault.storage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalFaultInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GlobalFaultInterceptor.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        log.error("Untracked exception caught in Storage Service", ex);
        
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", 400);
        errorBody.put("timestamp", System.currentTimeMillis());
        errorBody.put("fault", "SEC_FAULT_TRIGGERED");
        errorBody.put("identifier", "ASSET_LAYER");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }
}
