package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.api.TransactionRequest;
import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.model.Transaction;
import com.anshdesai.finpilot.repository.CategoryRepository;
import com.anshdesai.finpilot.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.anshdesai.finpilot.service.RuleEngineService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;

@Service
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final RuleEngineService ruleEngine;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository, RuleEngineService ruleEngine) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.ruleEngine = ruleEngine;
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found")
                );
    }

    public Transaction createTransaction(TransactionRequest req) {
        Transaction t = new Transaction(req.getDate(), req.getAmount(), req.getMerchant(), req.getCategory());

        // 1) Explicit categoryId from client → set + lock
        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            t.setCategoryRef(cat);
            t.setCategoryLocked(true);
        } else {
            // 2) No explicit category → try rules
            ruleEngine.apply(req.getMerchant()).ifPresent(cat -> {
                t.setCategoryRef(cat);
                t.setCategoryLocked(true); // recommended: lock after auto-assign
            });
            // If no match, leave categoryRef null and unlocked=false or true? Your call.
            if (t.getCategoryRef() == null) {
                t.setCategoryLocked(false);
            }
        }

        return transactionRepository.save(t);
    }

    public Transaction updateTransactionById(Long id, TransactionRequest req) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        boolean merchantChanged = (req.getMerchant() != null)
                && !req.getMerchant().equals(existing.getMerchant());

        // Basic field updates
        existing.setDate(req.getDate());
        existing.setAmount(req.getAmount());
        existing.setMerchant(req.getMerchant());
        existing.setCategory(req.getCategory()); // legacy text stays for now

        if (req.getCategoryId() != null) {
            // explicit override → set + lock
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            existing.setCategoryRef(cat);
            existing.setCategoryLocked(true);
        } else if (merchantChanged) {
            // merchant changed and no explicit category sent → re-run rules (unless you want to keep locked)
            existing.setCategoryLocked(false); // allow re-categorization
            ruleEngine.apply(req.getMerchant()).ifPresent(cat -> {
                existing.setCategoryRef(cat);
                existing.setCategoryLocked(true);
            });
        }
        // else: no explicit category, merchant not changed → keep current locked state as-is

        return transactionRepository.save(existing);
    }

    public void deleteTransactionById(Long id) {
        transactionRepository.deleteById(id);
    }

    public Page<Transaction> searchTransactions(
            String category,
            String q,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Specification<Transaction> spec = Specification.unrestricted();

        if (category != null && !category.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category"), category));
        }

        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("merchant")), like),
                            cb.like(cb.lower(root.get("category")), like)
                    ));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("date"), endDate));
        }

        return transactionRepository.findAll(spec, pageable);
    }
}
