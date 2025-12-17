package com.anshdesai.backend.controller;

import com.anshdesai.backend.model.Transaction;
import com.anshdesai.backend.model.User;
import com.anshdesai.backend.repository.TransactionRepository;
import com.anshdesai.backend.repository.UserRepository;
import com.anshdesai.backend.service.TransactionService;
import com.anshdesai.backend.service.TransactionSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionSyncService transactionSyncService;
    private final TransactionService transactionService;
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

    @PatchMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody TransactionService.UpdateTransactionRequest request) {
        // Get user email from authentication
        String email = authentication.getName();

        // Load user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update transaction using service
        Transaction updated = transactionService.updateTransaction(user, id, request);

        return ResponseEntity.ok(updated);
    }
}

