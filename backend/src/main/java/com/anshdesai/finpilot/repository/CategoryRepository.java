package com.anshdesai.finpilot.repository;

import com.anshdesai.finpilot.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByUserIdOrderByName(String userId);
    Optional<Category> findByIdAndUserId(UUID id, String userId);
}