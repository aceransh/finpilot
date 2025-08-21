package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final CurrentUser currentUser;

    public AuthController(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @GetMapping("/whoami")
    public Map<String, Object> whoami(HttpServletRequest req) {
        String uid   = currentUser.userId();
        String email = currentUser.getEmail();         // may be null in demo
        String name  = currentUser.getDisplayName();   // may be null in demo

        String demoHeader = req.getHeader("X-Demo-User");
        String authz = req.getHeader("Authorization");
        String mode = (authz != null && authz.startsWith("Bearer ")) ? "firebase" : "demo";

        return Map.of(
                "uid", uid,
                "mode", mode,
                "email", email == null ? "" : email,
                "displayName", name == null ? "" : name,
                "demoHeader", demoHeader == null ? "" : demoHeader
        );
    }
}