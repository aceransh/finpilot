package com.anshdesai.finpilot.service;

import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.anshdesai.finpilot.config.PlaidProperties;
import com.anshdesai.finpilot.api.PlaidPublicTokenExchangeResponse;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import com.anshdesai.finpilot.model.PlaidItem;
import com.anshdesai.finpilot.repository.PlaidItemRepository;
import com.plaid.client.model.AccountsGetRequest;
import com.plaid.client.model.AccountsGetResponse;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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

    public List<String> getAccountsForItem(String userId, UUID itemDbId) throws Exception {
        // 1) Load item scoped to the user
        var item = plaidItemRepo.findByIdAndUserId(itemDbId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        // 2) Decrypt the access token
        String accessToken = crypto.decrypt(item.getAccessTokenEnc());

        // 3) Call Plaid /accounts/get
        AccountsGetRequest req = new AccountsGetRequest().accessToken(accessToken);
        Response<AccountsGetResponse> resp = plaidApi.accountsGet(req).execute();

        if (!resp.isSuccessful() || resp.body() == null) {
            String err = (resp.errorBody() != null) ? resp.errorBody().string() : "unknown";
            throw new Exception("accountsGet failed: " + err);
        }

        // 4) Return a simple shape for now (name + mask)
        return resp.body().getAccounts().stream()
                .map(a -> {
                    String name = a.getName() != null ? a.getName() : a.getOfficialName();
                    String mask = a.getMask();
                    String subtype = (a.getSubtype() != null) ? a.getSubtype().getValue() : "";
                    return (name != null ? name : "Account") +
                            (mask != null ? " ••••" + mask : "") +
                            (subtype.isBlank() ? "" : " (" + subtype + ")");
                })
                .toList();
    }

    public List<Map<String, Object>> probeTransactions(String userId, UUID itemDbId, int days) throws Exception {
        // load the item for this user; 404-style error if not found
        var item = plaidItemRepo.findByIdAndUserId(itemDbId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        // decrypt the access token we stored
        String accessToken = crypto.decrypt(item.getAccessTokenEnc());

        // build a small date window: last N days (default later will be 30)
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(Math.max(1, days));
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        // prepare the Plaid request: cap at 5 results just to validate the pipe
        TransactionsGetRequest req = new TransactionsGetRequest()
                .accessToken(accessToken)
                .startDate(start)
                .endDate(end)
                .options(new TransactionsGetRequestOptions()
                        .includePersonalFinanceCategory(true)
                );

        // call Plaid
        Response<TransactionsGetResponse> resp = plaidApi.transactionsGet(req).execute();
        if (!resp.isSuccessful() || resp.body() == null) {
            String err = (resp.errorBody() != null) ? resp.errorBody().string() : "unknown";
            throw new Exception("transactionsGet failed: " + err);
        }

        // return a lean shape for easy eyeballing in HTTP client
        return resp.body().getTransactions().stream().map(t -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("date", t.getDate());
            m.put("amount", t.getAmount());
            m.put("name", t.getName());
            m.put("merchantName", t.getMerchantName());
            m.put("plaid_transaction_id", t.getTransactionId());
            m.put("account_id", t.getAccountId());
            m.put("pending", Boolean.TRUE.equals(t.getPending()));
            m.put("iso_currency", t.getIsoCurrencyCode());

            var pfc = t.getPersonalFinanceCategory();
            if (pfc != null) {
                Map<String, Object> pfcMap = new java.util.LinkedHashMap<>();
                pfcMap.put("primary", pfc.getPrimary());
                pfcMap.put("detailed", pfc.getDetailed());
                m.put("personal_finance_category", pfcMap);
            }
            return m;
        }).toList();
    }
}