package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.PlaidPublicTokenExchangeRequest;
import com.anshdesai.finpilot.service.PlaidService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.anshdesai.finpilot.security.CurrentUser;
import com.anshdesai.finpilot.api.PlaidSyncResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            body.put("error", ex.getMessage()); // <-- use 'ex'
            return ResponseEntity.status(502).body(body);
        }
    }

    @PostMapping("/public-token/exchange")
    public ResponseEntity<?> exchange(@RequestBody PlaidPublicTokenExchangeRequest body) {
        Map<String, Object> out = new java.util.HashMap<>();
        try {
            String userId = currentUser.userId();
            var resp = plaidService.exchangePublicToken(userId, body.getPublicToken());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            out.put("ok", false);
            out.put("error", ex.getMessage()); // <- show actual cause
            return ResponseEntity.status(502).body(out);
        }
    }

    @GetMapping("/items/{id}/accounts")
    public List<String> getAccountsForItem(@PathVariable UUID id) throws Exception {
        String userId = currentUser.userId();
        return plaidService.getAccountsForItem(userId, id);
    }

    @GetMapping("/items/{id}/probe-transactions")
    public List<Map<String, Object>> probeTransactions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "30") int days
    ) throws Exception {
        String userId = currentUser.userId();
        return plaidService.probeTransactions(userId, id, days);
    }

    @PostMapping("/items/{id}/sync")
    public PlaidSyncResponse syncItem(
            @PathVariable UUID id
    ) throws Exception {
        String userId = currentUser.userId();
        return plaidService.transactionsSync(userId, id);
    }
}
