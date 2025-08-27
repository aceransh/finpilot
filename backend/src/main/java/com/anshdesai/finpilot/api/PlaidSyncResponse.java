package com.anshdesai.finpilot.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

/**
 * Generic result for "we pulled transactions".
 * Works for both legacy date-window fetches and cursor-based sync.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // hide null fields (e.g., dates in sync)
public class PlaidSyncResponse {
    private int created;
    private int updated;
    private int removed;           // for /transactions/sync
    private Integer skipped;       // keep if you still use probe/get and want to report it
    private Boolean moreAvailable; // Plaid sync flag

    // Optional for legacy "get" shape
    private LocalDate startDate;
    private LocalDate endDate;

    public PlaidSyncResponse() {}

    // Constructor for sync use
    public PlaidSyncResponse(int created, int updated, int removed, Boolean moreAvailable) {
        this.created = created;
        this.updated = updated;
        this.removed = removed;
        this.moreAvailable = moreAvailable;
    }

    // Constructor for legacy get/probe use
    public PlaidSyncResponse(int created, int updated, int skipped, LocalDate startDate, LocalDate endDate) {
        this.created = created;
        this.updated = updated;
        this.skipped = skipped;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getCreated() { return created; }
    public void setCreated(int created) { this.created = created; }

    public int getUpdated() { return updated; }
    public void setUpdated(int updated) { this.updated = updated; }

    public int getRemoved() { return removed; }
    public void setRemoved(int removed) { this.removed = removed; }

    public Integer getSkipped() { return skipped; }
    public void setSkipped(Integer skipped) { this.skipped = skipped; }

    public Boolean getMoreAvailable() { return moreAvailable; }
    public void setMoreAvailable(Boolean moreAvailable) { this.moreAvailable = moreAvailable; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}