package com.anshdesai.backend.repository;

import com.anshdesai.backend.model.PlaidItem;
import com.anshdesai.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaidItemRepository extends JpaRepository<PlaidItem, UUID> {
    List<PlaidItem> findByUser(User user);
}

