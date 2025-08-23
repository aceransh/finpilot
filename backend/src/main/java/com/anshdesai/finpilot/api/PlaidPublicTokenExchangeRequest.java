package com.anshdesai.finpilot.api;

/** Request body from FE containing the short-lived public_token from Plaid Link. */
public class PlaidPublicTokenExchangeRequest {
    private String publicToken;

    public String getPublicToken() { return publicToken; }
    public void setPublicToken(String publicToken) { this.publicToken = publicToken; }
}
