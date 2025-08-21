package com.anshdesai.finpilot.config;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class PlaidConfig {

    private final PlaidProperties props;

    @Autowired
    public PlaidConfig(PlaidProperties props) {
        this.props = props;
    }

    @Bean
    public PlaidApi plaidApi() {
        // 1) Choose environment adapter
        String env = props.getEnv() == null ? "sandbox" : props.getEnv().toLowerCase();
        String adapter = switch (env) {
            case "production", "prod" -> ApiClient.Production;
            default -> ApiClient.Sandbox; // fallback to Sandbox
        };

        // 2) Add credentials + version
        java.util.HashMap<String, String> apiKeys = new java.util.HashMap<>();
        apiKeys.put("clientId", props.getClientId());
        apiKeys.put("secret", props.getSecret());
        apiKeys.put("plaidVersion", "2020-09-14"); // required version header

        // 3) Build ApiClient and set base URL
        ApiClient apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(adapter);

        // 4) Return typed API service
        return apiClient.createService(PlaidApi.class);
    }
}