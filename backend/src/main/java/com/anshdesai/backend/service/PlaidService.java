package com.anshdesai.backend.service;

import com.anshdesai.backend.model.PlaidItem;
import com.anshdesai.backend.model.User;
import com.anshdesai.backend.repository.PlaidItemRepository;
import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class PlaidService {

    private final PlaidApi plaidApi;
    private final PlaidItemRepository plaidItemRepository;

    public String createLinkToken(User user) {
        try {
            LinkTokenCreateRequestUser plaidUser = new LinkTokenCreateRequestUser()
                    .clientUserId(user.getId().toString());

            LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                    .user(plaidUser)
                    .clientName("FinPilot")
                    .products(Arrays.asList(Products.AUTH, Products.TRANSACTIONS))
                    .countryCodes(Collections.singletonList(CountryCode.US))
                    .language("en");

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
            ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest()
                    .publicToken(publicToken);

            Response<ItemPublicTokenExchangeResponse> response = plaidApi
                    .itemPublicTokenExchange(request)
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                PlaidItem item = new PlaidItem();
                item.setUser(user);
                item.setAccessToken(response.body().getAccessToken());
                item.setItemId(response.body().getItemId());
                item.setInstitutionName("Sandbox Bank"); // Now this will work!
                item.setStatus("ACTIVE");

                plaidItemRepository.save(item);
            } else {
                throw new RuntimeException("Plaid Error: " + (response.errorBody() != null ? response.errorBody().string() : "Unknown"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error exchanging token", e);
        }
    }

    // Fixed: Uses the startDate/endDate arguments you pass in
    public TransactionsGetResponse getTransactions(String accessToken, LocalDate startDate, LocalDate endDate) {
        try {
            TransactionsGetRequest request = new TransactionsGetRequest()
                    .accessToken(accessToken)
                    .startDate(startDate)
                    .endDate(endDate);

            Response<TransactionsGetResponse> response = plaidApi
                    .transactionsGet(request)
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                // Safe error handling to avoid warnings
                String errorMsg = "Unknown error";
                if (response.errorBody() != null) {
                    errorMsg = response.errorBody().string();
                }
                throw new RuntimeException("Error fetching transactions: " + errorMsg);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching transactions", e);
        }
    }
}