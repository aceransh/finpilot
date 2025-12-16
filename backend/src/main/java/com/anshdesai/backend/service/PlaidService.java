package com.anshdesai.backend.service;

import com.anshdesai.backend.model.User;
import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.*; // Imports all models (Request, Response, Products, etc.)
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.util.Arrays;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class PlaidService {

    // We inject the Interface now, not the Client class
    private final PlaidApi plaidApi;

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
}