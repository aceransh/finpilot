package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.api.TransactionRequest;
import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.model.Transaction;
import com.anshdesai.finpilot.api.TransactionMapper;
import com.anshdesai.finpilot.repository.CategoryRepository;
import com.anshdesai.finpilot.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;
import com.anshdesai.finpilot.security.CurrentUser;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.math.BigDecimal;

@Service
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final RuleEngineService ruleEngine;
    private final CurrentUser currentUser;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository, RuleEngineService ruleEngine, CurrentUser currentUser) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.ruleEngine = ruleEngine;
        this.currentUser = currentUser;
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findByIdAndUserId(id, currentUser.userId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found")
                );
    }

    public Transaction createTransaction(TransactionRequest req) {
        return createTransaction(req, false);                 // <-- default path: do NOT force, will 409 on dupes
    }

    public Transaction createTransaction(TransactionRequest req, boolean force) {
        // 1) Normalize merchant once (handles trailing spaces & weird whitespace)
        final String rawMerchant = req.getMerchant();
        final String normMerchant = rawMerchant == null ? "" : rawMerchant.trim();

        // 2) Dedupe (still honors `force`)
        if (!force) {
            boolean dup = transactionRepository.existsDuplicate(
                    req.getDate(), req.getAmount(), normMerchant, currentUser.userId()   // <-- use trimmed merchant
            );
            if (dup) {
                Optional<Transaction> existingOpt = transactionRepository.findFirstDuplicate(
                        req.getDate(), req.getAmount(), normMerchant, currentUser.userId() // <-- use trimmed merchant
                );

                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setTitle("Duplicate transaction");
                pd.setDetail("A matching transaction already exists.");
                pd.setProperty("code", "DUPLICATE");
                existingOpt.ifPresent(existing ->
                        pd.setProperty("existing", TransactionMapper.toResponse(existing))
                );
                pd.setProperty("candidate", Map.of(
                        "date", req.getDate(),
                        "amount", req.getAmount(),
                        "merchant", normMerchant                     // <-- normalized in the echo
                ));

                throw new ErrorResponseException(HttpStatus.CONFLICT, pd, null);
            }
        }

        // 3) Build entity (save the normalized merchant)
        Transaction t = new Transaction(req.getDate(), req.getAmount(), normMerchant, req.getCategory());
        t.setUserId(currentUser.userId());

        // 4) Manual category override via categoryId (unchanged)
        if (req.getCategoryId() != null) {
            UUID id = req.getCategoryId();
            Category cat = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            t.setCategoryRef(cat);
            t.setCategoryLocked(true);
            t.setCategory(cat.getName()); // mirror FK into legacy text for now
        } else {
            // 5) Try rules SAFELY (no 500s if a rule is bad). Use normalized merchant.
            t.setCategoryLocked(false);
            try {
                ruleEngine.apply(normMerchant).ifPresent(cat -> {
                    t.setCategoryRef(cat);
                    t.setCategory(cat.getName());  // mirror FK into legacy text
                    t.setCategoryLocked(true);     // lock because a rule decided it
                });
            } catch (RuntimeException e) {
                // Log & proceed without rules (prevents ALL-CAPS or bad regex from crashing)
                System.err.println("Rules engine error: " + e.getMessage());
            }
        }

        return transactionRepository.save(t);
    }

    public Transaction updateTransactionById(Long id, TransactionRequest req) {
        return updateTransactionById(id, req, false);
    }

    public Transaction updateTransactionById(Long id, TransactionRequest req, boolean force) {
        Transaction existing = transactionRepository.findByIdAndUserId(id, currentUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        // Build the “candidate” values from the request
        LocalDate date = req.getDate();
        BigDecimal amount = req.getAmount();
        String merchantRaw = req.getMerchant();

        // Normalize merchant for duplicate logic (same way you do it on create)
        String merchantNorm = RuleEngineService.normalize(merchantRaw);

        // Check duplicates EXCLUDING this row
        boolean dup = transactionRepository.existsDuplicateExcludingId(
                id, date, amount, merchantNorm, currentUser.userId()
        );

        if (dup && !force) {
            // Find the conflicting one to include in the response
            Transaction conflict = transactionRepository.findFirstDuplicateExcludingId(
                    id, date, amount, merchantNorm, currentUser.userId()
            ).orElse(null);

            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
            pd.setTitle("Duplicate transaction");
            pd.setDetail("Editing would create a duplicate.");
            pd.setProperty("code", "DUPLICATE");
            if (conflict != null) {
                pd.setProperty("existing", TransactionMapper.toResponse(conflict));
            }
            Map<String,Object> candidate = new LinkedHashMap<>();
            candidate.put("merchant", merchantRaw);
            candidate.put("date", date);
            candidate.put("amount", amount);
            pd.setProperty("candidate", candidate);

            throw new ErrorResponseException(HttpStatus.CONFLICT, pd, null);
        }

        // No duplicate (or forced) → apply updates
        existing.setDate(date);
        existing.setAmount(amount);
        existing.setMerchant(merchantNorm);
        existing.setUserId(currentUser.userId());

        // category handling: mirror your create logic
        if (req.getCategoryId() != null) {
            UUID catId = req.getCategoryId();
            Category cat = categoryRepository.findById(catId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            existing.setCategoryRef(cat);
            existing.setCategoryLocked(true);
        } else {
            // If user didn’t lock, optionally (re)run rules if you want:
            ruleEngine.applyCategoryIfUnlocked(existing);
        }

        return transactionRepository.save(existing);
    }

    public void deleteTransactionById(Long id) {
        Transaction existing = transactionRepository.findByIdAndUserId(id, currentUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transactionRepository.delete(existing);
    }

    public Page<Transaction> searchTransactions(
            String category,
            String q,
            LocalDate startDate,
            LocalDate endDate,
            String accountId,            // ← NEW
            Pageable pageable
    ) {
        Specification<Transaction> spec = (root, query, cb) ->
                cb.equal(root.get("userId"), currentUser.userId());

        if (category != null && !category.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
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
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate));
        }
        if (accountId != null && !accountId.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("plaidAccountId"), accountId));
        }

        return transactionRepository.findAll(spec, pageable);
    }
}
