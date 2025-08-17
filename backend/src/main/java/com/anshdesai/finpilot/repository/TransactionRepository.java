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

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    @Query("""
           SELECT t
           FROM Transaction t
           WHERE (:category IS NULL OR t.category = :category)
             AND (:q IS NULL OR LOWER(t.merchant) LIKE LOWER(CONCAT('%', :q, '%'))
                          OR LOWER(t.category) LIKE LOWER(CONCAT('%', :q, '%')))
             AND (:startDate IS NULL OR t.date >= :startDate)
             AND (:endDate   IS NULL OR t.date <= :endDate)
           """)
    Page<Transaction> search(@Param("category") String category,
                             @Param("q")        String q,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             Pageable pageable);

    @Query("""
       SELECT (COUNT(t) > 0)
       FROM Transaction t
       WHERE t.date = :date
         AND t.amount = :amount
         AND LOWER(t.merchant) = LOWER(:merchant)
       """)
    boolean existsDuplicate(@Param("date") LocalDate date,
                            @Param("amount") BigDecimal amount,
                            @Param("merchant") String merchant);

    @Query("""
       SELECT t
       FROM Transaction t
       WHERE t.date = :date
         AND t.amount = :amount
         AND LOWER(t.merchant) = LOWER(:merchant)
       """)
    Optional<Transaction> findFirstDuplicate(@Param("date") LocalDate date,
                                             @Param("amount") BigDecimal amount,
                                             @Param("merchant") String merchant);

    @Query("""
    SELECT (COUNT(t) > 0)
    FROM Transaction t
    WHERE t.id <> :id
      AND t.date = :date
      AND t.amount = :amount
      AND LOWER(TRIM(t.merchant)) = LOWER(TRIM(:merchant))
""")
    boolean existsDuplicateExcludingId(@Param("id") Long id,
                                       @Param("date") LocalDate date,
                                       @Param("amount") BigDecimal amount,
                                       @Param("merchant") String merchant);

    @Query("""
    SELECT t
    FROM Transaction t
    WHERE t.id <> :id
      AND t.date = :date
      AND t.amount = :amount
      AND LOWER(TRIM(t.merchant)) = LOWER(TRIM(:merchant))
""")
    Optional<Transaction> findFirstDuplicateExcludingId(@Param("id") Long id,
                                                        @Param("date") LocalDate date,
                                                        @Param("amount") BigDecimal amount,
                                                        @Param("merchant") String merchant);
}
