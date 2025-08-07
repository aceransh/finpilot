package com.anshdesai.finpilot.repository;

import com.anshdesai.finpilot.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
