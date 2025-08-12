package com.anshdesai.finpilot.api;

import com.anshdesai.finpilot.model.Transaction;
import com.anshdesai.finpilot.model.Category;

import java.util.List;
import java.util.stream.Collectors;

public final class TransactionMapper {
    private TransactionMapper() {}

    public static TransactionResponse toResponse(Transaction t) {
        Category c = t.getCategoryRef();
        String categoryId   = (c != null) ? c.getId().toString() : null;
        String categoryName = (c != null) ? c.getName() : t.getCategory(); // fallback to legacy string

        return new TransactionResponse(
                t.getId(),
                t.getDate(),
                t.getAmount(),
                t.getMerchant(),
                t.getCategory(),   // keep legacy field as-is for now
                categoryId,
                categoryName,
                t.isCategoryLocked()
        );
    }

    public static List<TransactionResponse> toResponseList(List<Transaction> list) {
        return list.stream().map(TransactionMapper::toResponse).collect(Collectors.toList());
    }
}