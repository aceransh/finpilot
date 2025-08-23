package com.anshdesai.finpilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

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

    private List<String> products;

    private List<String> countryCodes;

    // --- getters/setters (needed by Spring) ---
    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public List<String> getProducts() { return products; }
    public void setProducts(List<String> products) { this.products = products; }

    public List<String> getCountryCodes() { return countryCodes; }
    public void setCountryCodes(List<String> countryCodes) { this.countryCodes = countryCodes; }

    @Override
    public String toString() {
        String masked = (secret == null || secret.isBlank()) ? "MISSING" : "SET";
        return "PlaidProperties{env='" + env + "', clientId='" +
                (clientId == null ? "MISSING" : clientId) +
                "', secret=" + masked +
                ", products=" + products +
                ", countryCodes=" + countryCodes +
                "}";
    }
}