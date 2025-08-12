package com.anshdesai.finpilot.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class RuleRequest {
    @NotBlank private String pattern;
    @NotBlank private String matchType;     // "CONTAINS" | "REGEX"
    @NotNull  private UUID   categoryId;    // UUID in request -> no manual fromString needed
    private Integer priority;               // optional; default in controller if null
    private Boolean enabled;                // optional; default in controller if null

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}