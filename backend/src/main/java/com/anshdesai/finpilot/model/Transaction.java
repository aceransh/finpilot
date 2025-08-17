package com.anshdesai.finpilot.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private BigDecimal amount;
    private String merchant;

    // Legacy display/category text (keep for now)
    private String category;

    // NEW: real FK to categorize table (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")   // matches V2 column
    private Category categoryRef;

    // NEW: lock flag (default false in DB)
    @Column(name = "category_locked", nullable = false)
    private boolean categoryLocked = false;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    public Transaction() {}

    public Transaction(LocalDate date, BigDecimal amount, String merchant, String category) {
        this.date = date;
        this.amount = amount;
        this.merchant = merchant;
        this.category = category;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Category getCategoryRef() { return categoryRef; }
    public void setCategoryRef(Category categoryRef) { this.categoryRef = categoryRef; }

    public boolean isCategoryLocked() { return categoryLocked; }
    public void setCategoryLocked(boolean categoryLocked) { this.categoryLocked = categoryLocked; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}