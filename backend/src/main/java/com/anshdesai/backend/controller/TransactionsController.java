package com.anshdesai.backend.controller;

import com.anshdesai.backend.model.Transaction;
import com.anshdesai.backend.model.User;
import com.anshdesai.backend.repository.TransactionRepository;
import com.anshdesai.backend.repository.UserRepository;
import com.anshdesai.backend.service.TransactionSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionSyncService transactionSyncService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncTransactions(Authentication authentication) {
        // Get user email from authentication
        String email = authentication.getName();

        // Load user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Sync transactions
        int count = transactionSyncService.syncTransactions(user);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "count", count
        ));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(Authentication authentication) {
        // Get user email from authentication
        String email = authentication.getName();

        // Load user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get transactions for user
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByDateDesc(user.getId());

        return ResponseEntity.ok(transactions);
    }
}

