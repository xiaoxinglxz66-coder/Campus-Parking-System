package com.example.campusparkingbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class PasswordTestController {

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // 使用构造器注入，避免字段注入问题
    @Autowired
    public PasswordTestController(org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/password")
    public ResponseEntity<Map<String, Object>> testPassword(@RequestParam String password) {
        try {
            // 使用完整类名避免导入问题
            String encoded = passwordEncoder.encode(password);
            boolean matches = passwordEncoder.matches(password, encoded);

            Map<String, Object> result = new HashMap<>();
            result.put("input", password);
            result.put("encoded", encoded);
            result.put("matches", matches);
            result.put("status", "success");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
