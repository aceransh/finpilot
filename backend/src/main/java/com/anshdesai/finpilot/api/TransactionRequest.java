package com.anshdesai.finpilot.api;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;                 // <-- add this import

public class TransactionRequest {
    @NotNull private LocalDate date;
    @NotNull private BigDecimal amount;
    @NotNull private String     merchant;
    @NotNull private String     category;

    // NEW: optional link to categories table
    private UUID categoryId;         // <-- add this field

    public TransactionRequest(LocalDate date, BigDecimal amount, String merchant, String category) {
        this.date = date;
        this.amount = amount;
        this.merchant = merchant;
        this.category = category;
    }

    public TransactionRequest() {}   // <-- ensure a no-args ctor exists for Jackson

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // NEW: getters/setters
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
}