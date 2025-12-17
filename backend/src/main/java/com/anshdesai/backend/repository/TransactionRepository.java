package com.anshdesai.backend.repository;

import com.anshdesai.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByPlaidTransactionId(String plaidTransactionId);
    
    @Query("SELECT t FROM Transaction t " +
           "JOIN t.account a " +
           "JOIN a.plaidItem p " +
           "JOIN p.user u " +
           "WHERE u.id = :userId " +
           "ORDER BY t.date DESC")
    List<Transaction> findByUserIdOrderByDateDesc(@Param("userId") UUID userId);
}

