package com.anshdesai.backend.service;

import com.anshdesai.backend.model.Category;
import com.anshdesai.backend.model.Transaction;
import com.anshdesai.backend.model.User;
import com.anshdesai.backend.repository.CategoryRepository;
import com.anshdesai.backend.repository.TransactionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Transaction updateTransaction(User user, UUID transactionId, UpdateTransactionRequest request) {
        // Find transaction
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Verify user owns the transaction (Security Check)
        if (!transaction.getAccount().getPlaidItem().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Transaction does not belong to user");
        }

        // Update description if provided
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            // Verify category belongs to user (Security Check)
            if (!category.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized: Category does not belong to user");
            }

            transaction.setCategory(category);
        }

        return transactionRepository.save(transaction);
    }

    @Data
    public static class UpdateTransactionRequest {
        private UUID categoryId;
        private String description;
    }
}