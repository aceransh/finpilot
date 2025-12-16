package com.anshdesai.backend.controller;

import com.anshdesai.backend.model.User;
import com.anshdesai.backend.payload.AuthRequest;
import com.anshdesai.backend.payload.AuthResponse;
import com.anshdesai.backend.repository.UserRepository;
import com.anshdesai.backend.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        
        userRepository.save(user);
        
        String token = jwtUtils.generateToken(user.getEmail());
        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = jwtUtils.generateToken(user.getEmail());
        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }
}

