package com.anshdesai.backend.repository;

import com.anshdesai.backend.model.CategorizationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorizationRuleRepository extends JpaRepository<CategorizationRule, Long> {
}

