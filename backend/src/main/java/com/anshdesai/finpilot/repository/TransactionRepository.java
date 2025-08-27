package com.anshdesai.finpilot.repository;

import com.anshdesai.finpilot.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("""
       SELECT (COUNT(t) > 0)
       FROM Transaction t
       WHERE t.userId = :userId
         AND t.date = :date
         AND t.amount = :amount
         AND LOWER(t.merchant) = LOWER(:merchant)
       """)
    boolean existsDuplicate(@Param("date") LocalDate date,
                            @Param("amount") BigDecimal amount,
                            @Param("merchant") String merchant,
                            @Param("userId") String userId);

    @Query("""
       SELECT t
       FROM Transaction t
       WHERE t.userId = :userId
         AND t.date = :date
         AND t.amount = :amount
         AND LOWER(t.merchant) = LOWER(:merchant)
       """)
    Optional<Transaction> findFirstDuplicate(@Param("date") LocalDate date,
                                             @Param("amount") BigDecimal amount,
                                             @Param("merchant") String merchant,
                                             @Param("userId") String userId);

    @Query("""
       SELECT (COUNT(t) > 0)
       FROM Transaction t
       WHERE t.userId = :userId
         AND t.id <> :id
         AND t.date = :date
         AND t.amount = :amount
         AND LOWER(t.merchant) = LOWER(:merchant)
       """)
    boolean existsDuplicateExcludingId(@Param("id") Long id,
                                       @Param("date") LocalDate date,
                                       @Param("amount") BigDecimal amount,
                                       @Param("merchant") String merchant,
                                       @Param("userId") String userId);

    @Query("""
       SELECT t
       FROM Transaction t
       WHERE t.userId = :userId
         AND t.id <> :id
         AND t.date = :date
         AND t.amount = :amount
         AND LOWER(t.merchant) = LOWER(:merchant)
       """)
    Optional<Transaction> findFirstDuplicateExcludingId(@Param("id") Long id,
                                                        @Param("date") LocalDate date,
                                                        @Param("amount") BigDecimal amount,
                                                        @Param("merchant") String merchant,
                                                        @Param("userId") String userId);

    // Optional, but handy: fetch with owner check used by service
    Optional<Transaction> findByIdAndUserId(Long id, String userId);

    // (If you ever need it) page all by user id
    Page<Transaction> findAllByUserId(String userId, Pageable pageable);

    boolean existsByUserIdAndPlaidTransactionId(String userId, String plaidTransactionId);
}