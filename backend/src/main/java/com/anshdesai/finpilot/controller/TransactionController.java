package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.TransactionRequest;
import com.anshdesai.finpilot.api.TransactionResponse;
import com.anshdesai.finpilot.api.TransactionMapper;
import com.anshdesai.finpilot.model.Transaction;
import com.anshdesai.finpilot.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    private final TransactionService txService;

    public TransactionController(TransactionService txService) {
        this.txService = txService;
    }

    // GET /transactions?page=&size=&sort=&q=&category=&from=&to=
    @GetMapping
    public Page<TransactionResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault Pageable pageable
    ) {
        Page<Transaction> page = txService.searchTransactions(category, q, from, to, pageable);
        return page.map(TransactionMapper::toResponse);
    }

    // GET /transactions/{id}
    @GetMapping("/{id}")
    public TransactionResponse get(@PathVariable Long id) {
        Transaction t = txService.getTransactionById(id);
        return TransactionMapper.toResponse(t);
    }

    // POST /transactions?force=false
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(
            @Valid @RequestBody TransactionRequest body,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        Transaction t = txService.createTransaction(body, force);
        return TransactionMapper.toResponse(t);
    }

    // PUT /transactions/{id}?force=false
    @PutMapping("/{id}")
    public TransactionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest body,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        Transaction t = txService.updateTransactionById(id, body, force);
        return TransactionMapper.toResponse(t);
    }

    // DELETE /transactions/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        txService.deleteTransactionById(id);
    }
}