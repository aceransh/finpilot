package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.model.Transaction;
import com.anshdesai.finpilot.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found")
                );
    }

    public Transaction createTransaction(Transaction tx) {
        return transactionRepository.save(tx);
    }

    public Transaction updateTransactionById(Long id, Transaction updated) {
        Transaction transaction = getTransactionById(id);
        transaction.setAmount(updated.getAmount());
        transaction.setCategory(updated.getCategory());
        transaction.setDate(updated.getDate());
        return transaction;
    }

    public void deleteTransactionById(Long id) {
        transactionRepository.deleteById(id);
    }
}
