package com.anshdesai.finpilot.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {
    private long id;
    private LocalDate date;
    private BigDecimal amount;
    private String merchant;

    // legacy display string (keep for backwards-compat if you like)
    private String category;

    // NEW
    private String categoryId;     // UUID as string, nullable
    private String categoryName;   // resolved server-side
    private boolean categoryLocked;

    public TransactionResponse() {}

    public TransactionResponse(
            long id,
            LocalDate date,
            BigDecimal amount,
            String merchant,
            String category,
            String categoryId,
            String categoryName,
            boolean categoryLocked
    ) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.merchant = merchant;
        this.category = category;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryLocked = categoryLocked;
    }

    // getters/setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public boolean isCategoryLocked() { return categoryLocked; }
    public void setCategoryLocked(boolean categoryLocked) { this.categoryLocked = categoryLocked; }
}