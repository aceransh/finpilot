package com.anshdesai.backend.repository;

import com.anshdesai.backend.model.Category;
import com.anshdesai.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByNameAndUserId(String name, UUID userId);
    List<Category> findByUser(User user);
}

