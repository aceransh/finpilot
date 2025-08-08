package com.anshdesai.finpilot.api;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {
    @NotNull private LocalDate date;
    @NotNull private BigDecimal amount;
    @NotNull private String     merchant;
    @NotNull private String     category;

    public TransactionRequest(LocalDate date, BigDecimal amount, String merchant, String category) {
        this.date = date;
        this.amount = amount;
        this.merchant = merchant;
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
