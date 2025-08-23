package com.anshdesai.finpilot.api;

public class PlaidPublicTokenExchangeResponse {
    private boolean ok;
    private String itemId;     // Plaid item_id
    private String message;    // optional human-readable info

    public PlaidPublicTokenExchangeResponse() {}

    public PlaidPublicTokenExchangeResponse(boolean ok, String itemId, String message) {
        this.ok = ok;
        this.itemId = itemId;
        this.message = message;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
