package com.anshdesai.finpilot.repository;

import com.anshdesai.finpilot.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleRepository extends JpaRepository<Rule, UUID> {

    // Read all rules for a user ordered by priority (low number = higher priority)
    List<Rule> findAllByUserIdOrderByPriorityAsc(String userId);

    // Read only enabled rules for a user ordered by priority (used by rules engine)
    List<Rule> findAllByUserIdAndEnabledTrueOrderByPriorityAsc(String userId);

    // Load a single rule ensuring it belongs to the caller
    Optional<Rule> findByIdAndUserId(UUID id, String userId);

    // Useful when guarding category deletes (are there rules for this cat for this user?)
    boolean existsByCategory_IdAndUserId(UUID categoryId, String userId);
}