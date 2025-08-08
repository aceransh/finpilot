package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.TransactionRequest;
import com.anshdesai.finpilot.model.Transaction;
import com.anshdesai.finpilot.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction createTransaction(@Valid @RequestBody TransactionRequest req) {
        Transaction tx = new Transaction(req.getDate(), req.getAmount(), req.getMerchant(), req.getCategory());
        return transactionService.createTransaction(tx);
    }

    @PutMapping("/{id}")
    public Transaction updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionRequest req) {
        Transaction tx = new Transaction(req.getDate(), req.getAmount(), req.getMerchant(), req.getCategory());
        return transactionService.updateTransactionById(id, tx);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransactionById(@PathVariable Long id) {
        transactionService.deleteTransactionById(id);
    }
}
