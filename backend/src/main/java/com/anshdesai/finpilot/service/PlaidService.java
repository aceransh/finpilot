package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.api.PlaidPublicTokenExchangeRequest;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.anshdesai.finpilot.config.PlaidProperties;
import com.anshdesai.finpilot.api.PlaidPublicTokenExchangeResponse;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import com.anshdesai.finpilot.service.CryptoService;
import com.anshdesai.finpilot.model.PlaidItem;
import com.anshdesai.finpilot.repository.PlaidItemRepository;

import java.util.UUID;
import java.util.List;

@Service
public class PlaidService {
    private final PlaidApi plaidApi;
    private final PlaidProperties props;
    private final CryptoService crypto;
    private final PlaidItemRepository plaidItemRepo;

    public PlaidService(PlaidApi plaidApi, PlaidProperties props, CryptoService crypto, PlaidItemRepository plaidItemRepo) {
        this.plaidApi = plaidApi;
        this.props = props;
        this.crypto = crypto;
        this.plaidItemRepo = plaidItemRepo;
    }

    // tiny helper: ["US","CA"] -> [CountryCode.US, CountryCode.CA]
    private List<CountryCode> toCountryCodes(List<String> codes) {
        return codes == null ? List.of()
                : codes.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> CountryCode.valueOf(s.trim().toUpperCase()))
                .toList();
    }

    /** Simple sanity check against Plaid Institutions API */
    public String testConnection() throws Exception {
        InstitutionsGetRequest req = new InstitutionsGetRequest()
                .count(10)
                .offset(0)
                .countryCodes(toCountryCodes(props.getCountryCodes())); // ← REQUIRED

        Response<InstitutionsGetResponse> resp = plaidApi.institutionsGet(req).execute();

        if (resp.isSuccessful() && resp.body() != null && !resp.body().getInstitutions().isEmpty()) {
            return "Plaid OK. First institution: " + resp.body().getInstitutions().getFirst().getName();
        } else {
            String err = resp.errorBody() != null ? resp.errorBody().string() : "unknown";
            throw new Exception("Plaid connection failed: " + err);
        }
    }

    /** Create a Plaid link_token for the frontend */
    public String createLinkToken(String userId) throws Exception {
        LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
                .clientUserId(userId);

        LinkTokenCreateRequest req = new LinkTokenCreateRequest()
                .user(user)
                .clientName("Finpilot")
                .products(List.of(Products.TRANSACTIONS))
                .countryCodes(toCountryCodes(props.getCountryCodes()))
                .language("en");

        Response<LinkTokenCreateResponse> resp = plaidApi.linkTokenCreate(req).execute();

        if (resp.isSuccessful() && resp.body() != null) {
            return resp.body().getLinkToken();
        } else {
            String err = (resp.errorBody() != null) ? resp.errorBody().string() : "unknown error";
            throw new Exception("Plaid link_token create failed: " + err);
        }
    }

    public PlaidPublicTokenExchangeResponse exchangePublicToken(String userId, String publicToken) throws Exception {
        // 1) Call Plaid to exchange
        ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest().publicToken(publicToken);
        Response<ItemPublicTokenExchangeResponse> response = plaidApi.itemPublicTokenExchange(request).execute();

        if (!response.isSuccessful() || response.body() == null) {
            String err = (response.errorBody() != null) ? response.errorBody().string() : "unknown error";
            throw new Exception("Plaid public token exchange failed: " + err);
        }

        String accessToken = response.body().getAccessToken();  // plaintext from Plaid
        String itemId = response.body().getItemId();            // Plaid item_id

        // 2) Encrypt the token
        String accessTokenEnc = crypto.encrypt(accessToken);

        // 3) Upsert plaid_items for (userId, itemId)
        var existing = plaidItemRepo.findByPlaidItemIdAndUserId(itemId, userId);
        PlaidItem entity = existing.orElseGet(() -> {
            PlaidItem pi = new PlaidItem();
            pi.setId(UUID.randomUUID());
            pi.setUserId(userId);
            pi.setItemId(itemId);
            return pi;
        });

        entity.setAccessTokenEnc(accessTokenEnc);

        plaidItemRepo.save(entity);

        // 4) Return a lean response (never return the token)
        return new PlaidPublicTokenExchangeResponse(
                true,
                itemId,
                "Stored encrypted access token for item_id " + itemId
        );
    }
}