package com.anshdesai.backend.service;

import com.anshdesai.backend.model.PlaidItem;
import com.anshdesai.backend.model.User;
import com.anshdesai.backend.repository.PlaidItemRepository;
import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.*; // Imports all models (Request, Response, Products, etc.)
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class PlaidService {

    // We inject the Interface now, not the Client class
    private final PlaidApi plaidApi;
    private final PlaidItemRepository plaidItemRepository;

    public String createLinkToken(User user) {
        try {
            // 1. Create the User object required by Plaid
            LinkTokenCreateRequestUser plaidUser = new LinkTokenCreateRequestUser()
                    .clientUserId(user.getId().toString());

            // 2. Build the Request
            LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                    .user(plaidUser)
                    .clientName("FinPilot")
                    .products(Collections.singletonList(Products.AUTH))
                    .countryCodes(Collections.singletonList(CountryCode.US))
                    .language("en");

            // 3. Execute the call
            Response<LinkTokenCreateResponse> response = plaidApi
                    .linkTokenCreate(request)
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body().getLinkToken();
            } else {
                throw new RuntimeException("Plaid Error: " + (response.errorBody() != null ? response.errorBody().string() : "Unknown"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating link token", e);
        }
    }

    public void exchangePublicToken(User user, String publicToken) {
        try {
            // 1. Create the exchange request
            ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest()
                    .publicToken(publicToken);

            // 2. Execute the call
            Response<ItemPublicTokenExchangeResponse> response = plaidApi
                    .itemPublicTokenExchange(request)
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                ItemPublicTokenExchangeResponse exchangeResponse = response.body();
                String accessToken = exchangeResponse.getAccessToken();
                String itemId = exchangeResponse.getItemId();

                // 3. Create and save PlaidItem entity
                PlaidItem plaidItem = PlaidItem.builder()
                        .user(user)
                        .accessToken(accessToken)
                        .itemId(itemId)
                        .status("ACTIVE")
                        .build();

                plaidItemRepository.save(plaidItem);
            } else {
                throw new RuntimeException("Plaid Error: " + (response.errorBody() != null ? response.errorBody().string() : "Unknown"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error exchanging public token", e);
        }
    }
}