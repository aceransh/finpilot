package com.anshdesai.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plaid_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaidItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    // --- ADD THIS NEW FIELD ---
    @Column(name = "institution_name")
    private String institutionName;

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "plaidItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;
}