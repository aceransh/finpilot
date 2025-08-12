package com.anshdesai.finpilot.repository;

import com.anshdesai.finpilot.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RuleRepository extends JpaRepository<Rule, UUID> {
    List<Rule> findAllByOrderByPriorityAsc();
    List<Rule> findAllByEnabledTrueOrderByPriorityAsc();
    boolean existsByCategory_Id(UUID categoryId);
}