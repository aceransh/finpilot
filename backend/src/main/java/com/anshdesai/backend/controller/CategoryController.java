package com.anshdesai.backend.controller;

import com.anshdesai.backend.model.Category;
import com.anshdesai.backend.model.Transaction;
import com.anshdesai.backend.model.User;
import com.anshdesai.backend.repository.CategoryRepository;
import com.anshdesai.backend.repository.TransactionRepository;
import com.anshdesai.backend.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Category> categories = categoryRepository.findByUser(user);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            Authentication authentication,
            @RequestBody CreateCategoryRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if category with same name already exists for this user
        if (categoryRepository.findByNameAndUserId(request.getName(), user.getId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .colorHex(request.getColorHex() != null ? request.getColorHex() : "#2979ff")
                .build();

        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Verify category belongs to user
        if (!category.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update fields
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getColorHex() != null) {
            category.setColorHex(request.getColorHex());
        }

        Category updated = categoryRepository.save(category);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            Authentication authentication,
            @PathVariable Long id) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Verify category belongs to user
        if (!category.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Set transactions' category to null before deleting to avoid FK errors
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(txn -> txn.getCategory() != null && txn.getCategory().getId().equals(id))
                .toList();

        for (Transaction transaction : transactions) {
            transaction.setCategory(null);
            transactionRepository.save(transaction);
        }

        categoryRepository.delete(category);
        return ResponseEntity.noContent().build();
    }

    @Data
    static class CreateCategoryRequest {
        private String name;
        private String colorHex;
    }

    @Data
    static class UpdateCategoryRequest {
        private String name;
        private String colorHex;
    }
}

