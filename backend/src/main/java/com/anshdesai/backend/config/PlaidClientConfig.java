package com.anshdesai.backend.config;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class PlaidClientConfig {

    @Value("${plaid.client.id}")
    private String clientId;

    @Value("${plaid.client.secret}")
    private String clientSecret;

    @Bean
    public PlaidApi plaidApi() {
        HashMap<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", clientId);
        apiKeys.put("secret", clientSecret);

        ApiClient apiClient = new ApiClient(apiKeys);

        // Strictly set to Sandbox for now
        apiClient.setPlaidAdapter(ApiClient.Sandbox);

        return apiClient.createService(PlaidApi.class);
    }
}