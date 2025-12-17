package com.anshdesai.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // <--- Safety Import

import java.util.UUID; // <--- UUID Import

@Entity
@Table(name = "categorization_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorizationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // <--- FIXED: Matching Database
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // <--- FIXED: Prevents infinite loop
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore // <--- FIXED: Prevents infinite loop
    private Category category;

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false)
    private MatchType matchType;

    // Enum definition included here for completeness
    public enum MatchType {
        CONTAINS, EXACT, STARTS_WITH, REGEX
    }
}