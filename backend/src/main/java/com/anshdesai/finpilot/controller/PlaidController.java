package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.PlaidPublicTokenExchangeRequest;
import com.anshdesai.finpilot.api.PlaidPublicTokenExchangeResponse;
import com.anshdesai.finpilot.service.PlaidService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.anshdesai.finpilot.security.CurrentUser;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/plaid")
public class PlaidController {
    private final PlaidService plaidService;
    private final CurrentUser currentUser;

    public PlaidController(PlaidService plaidService, CurrentUser currentUser) {
        this.plaidService = plaidService;
        this.currentUser = currentUser;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> body = new HashMap<>();
        try {
            plaidService.testConnection();
            body.put("ok", true);
            body.put("message", "Plaid connection works.");
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            body.put("ok", false);
            body.put("error", ex.getMessage());
            return ResponseEntity.status(502).body(body);
        }
    }

    @PostMapping("/link/token/create")
    public ResponseEntity<Map<String, Object>> createLinkToken() {
        Map<String, Object> body = new HashMap<>();
        try {
            String uid = currentUser.userId();
            String linkToken = plaidService.createLinkToken(uid);
            body.put("link_token", linkToken);
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            body.put("error", ex.getMessage());
            return ResponseEntity.status(502).body(body);
        }
    }

    @PostMapping("/public-token/exchange")
    public PlaidPublicTokenExchangeResponse exchange(@RequestBody PlaidPublicTokenExchangeRequest body) throws Exception {
        String userId = currentUser.userId(); // from your request-scoped bean
        return plaidService.exchangePublicToken(userId, body.getPublicToken());
    }
}
