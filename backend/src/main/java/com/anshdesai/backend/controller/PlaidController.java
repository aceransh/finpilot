package com.anshdesai.backend.controller;

import com.anshdesai.backend.model.User;
import com.anshdesai.backend.repository.UserRepository;
import com.anshdesai.backend.service.PlaidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/plaid")
@RequiredArgsConstructor
public class PlaidController {
    
    private final PlaidService plaidService;
    private final UserRepository userRepository;
    
    @PostMapping("/link-token")
    public ResponseEntity<Map<String, String>> createLinkToken(Authentication authentication) {
        // Get user email from authentication
        String email = authentication.getName();
        
        // Load user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create link token
        String linkToken = plaidService.createLinkToken(user);
        
        return ResponseEntity.ok(Map.of("link_token", linkToken));
    }
}

