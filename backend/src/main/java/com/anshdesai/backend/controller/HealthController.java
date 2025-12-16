package com.anshdesai.backend.controller;

import com.anshdesai.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {
    
    private final UserRepository userRepository;
    
    @GetMapping
    public Map<String, Object> health() {
        long count = userRepository.count();
        return Map.of(
            "status", "UP",
            "db_check", count
        );
    }
}

