package com.anshdesai.finpilot.repository;

import com.anshdesai.finpilot.model.PlaidItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaidItemRepository extends JpaRepository<PlaidItem, UUID> {

    // [1] All items for the signed-in user
    List<PlaidItem> findAllByUserIdOrderByCreatedAtDesc(String userId);

    // [2] Single item by DB id, scoped to user
    Optional<PlaidItem> findByIdAndUserId(UUID id, String userId);

    // [3] Lookup by Plaid's item_id, scoped to user
    Optional<PlaidItem> findByPlaidItemIdAndUserId(String plaidItemId, String userId);

    // [4] Useful for guarding duplicates
    boolean existsByPlaidItemIdAndUserId(String plaidItemId, String userId);
}