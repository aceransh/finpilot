package com.anshdesai.finpilot.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(nullable = false)
    private String pattern;          // e.g. "harris teeter" or a regex

    @Column(name = "match_type", nullable = false)
    private String matchType;        // "CONTAINS" | "REGEX"

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;       // FK to categories

    @Column(nullable = false)
    private int priority = 100;      // lower = higher priority

    @Column(nullable = false)
    private boolean enabled = true;

    // --- getters/setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}