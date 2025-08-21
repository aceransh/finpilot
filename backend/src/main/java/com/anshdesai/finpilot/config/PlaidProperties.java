package com.anshdesai.finpilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds to:
 *   app.plaid.env
 *   app.plaid.client-id
 *   app.plaid.secret
 *
 * Values are supplied via environment variables in IntelliJ.
 */
@ConfigurationProperties(prefix = "app.plaid")
public class PlaidProperties {

    // app.plaid.env (defaults to "sandbox" in application.properties)
    private String env;

    // app.plaid.client-id (no default, must be provided)
    private String clientId;

    // app.plaid.secret (no default, must be provided)
    private String secret;

    // --- getters/setters (needed by Spring) ---
    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    @Override
    public String toString() {
        // Never log the secret. Show a hint that it is present.
        String masked = (secret == null || secret.isBlank()) ? "MISSING" : "SET";
        return "PlaidProperties{env='" + env + "', clientId='" +
                (clientId == null ? "MISSING" : clientId) + "', secret=" + masked + "}";
    }
}