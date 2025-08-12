package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.TransactionMapper;
import com.anshdesai.finpilot.api.TransactionRequest;
import com.anshdesai.finpilot.api.TransactionResponse;
import com.anshdesai.finpilot.model.Transaction;
import com.anshdesai.finpilot.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public Page<TransactionResponse> getTransactions(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to") String toStr,
            Pageable pageable
    ) {
        // Accept empty strings from the client without blowing up
        LocalDate startDate = (fromStr != null && !fromStr.isBlank()) ? LocalDate.parse(fromStr) : null;
        LocalDate endDate   = (toStr   != null && !toStr.isBlank())   ? LocalDate.parse(toStr)   : null;

        return transactionService
                .searchTransactions(category, q, startDate, endDate, pageable)
                .map(TransactionMapper::toResponse);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransactionById(@PathVariable Long id) {
        return TransactionMapper.toResponse(transactionService.getTransactionById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@Valid @RequestBody TransactionRequest tx) {
        return TransactionMapper.toResponse(transactionService.createTransaction(tx));
    }

    @PutMapping("/{id}")
    public TransactionResponse updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionRequest tx) {
        return TransactionMapper.toResponse(transactionService.updateTransactionById(id, tx));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransactionById(@PathVariable Long id) {
        transactionService.deleteTransactionById(id);
    }
}