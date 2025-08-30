package com.anshdesai.finpilot.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PlaidItemSummary {
    private final UUID id;                 // DB UUID
    private final String plaidItemId;      // Plaid item_id
    private final String institutionId;    // e.g. ins_109508
    private final String institutionName;  // e.g. First Platypus Bank
    private final OffsetDateTime createdAt;

    public PlaidItemSummary(
            UUID id,
            String plaidItemId,
            String institutionId,
            String institutionName,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.plaidItemId = plaidItemId;
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getPlaidItemId() { return plaidItemId; }
    public String getInstitutionId() { return institutionId; }
    public String getInstitutionName() { return institutionName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}