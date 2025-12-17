package com.anshdesai.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // <--- IMPORT THIS

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Standardized to UUID
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore // <--- CUT LOOP 1: Don't load the User
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // <--- CUT LOOP 2: Don't load Rules
    private List<CategorizationRule> categorizationRules;

    @OneToMany(mappedBy = "category")
    @JsonIgnore // <--- CUT LOOP 3: Don't load Transactions (Critical!)
    private List<Transaction> transactions;
}