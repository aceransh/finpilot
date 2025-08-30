package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.api.PlaidItemSummary;
import com.anshdesai.finpilot.api.PlaidPublicTokenExchangeResponse;
import com.anshdesai.finpilot.api.PlaidSyncResponse;
import com.anshdesai.finpilot.config.PlaidProperties;
import com.anshdesai.finpilot.model.PlaidItem;
import com.anshdesai.finpilot.repository.PlaidItemRepository;
import com.anshdesai.finpilot.repository.TransactionRepository;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.ItemGetRequest;
import com.plaid.client.model.ItemGetResponse;
import com.plaid.client.model.InstitutionsGetByIdRequest;
import com.plaid.client.model.InstitutionsGetByIdResponse;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlaidService {
    private final PlaidApi plaidApi;
    private final PlaidProperties props;
    private final CryptoService crypto;
    private final PlaidItemRepository plaidItemRepo;
    private final TransactionRepository txRepo;

    public PlaidService(
            PlaidApi plaidApi,
            PlaidProperties props,
            CryptoService crypto,
            PlaidItemRepository plaidItemRepo,
            TransactionRepository txRepo
    ) {
        this.plaidApi = plaidApi;
        this.props = props;
        this.crypto = crypto;
        this.plaidItemRepo = plaidItemRepo;
        this.txRepo = txRepo;
    }

    private List<CountryCode> toCountryCodes(List<String> codes) {
        return codes == null ? List.of()
                : codes.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> CountryCode.valueOf(s.trim().toUpperCase()))
                .toList();
    }

    /** Quick sanity check */
    public String testConnection() throws Exception {
        InstitutionsGetRequest req = new InstitutionsGetRequest()
                .count(10)
                .offset(0)
                .countryCodes(toCountryCodes(props.getCountryCodes()));
        Response<InstitutionsGetResponse> resp = plaidApi.institutionsGet(req).execute();
        if (resp.isSuccessful() && resp.body() != null && !resp.body().getInstitutions().isEmpty()) {
            return "Plaid OK. First institution: " + resp.body().getInstitutions().getFirst().getName();
        }
        String err = resp.errorBody() != null ? resp.errorBody().string() : "unknown";
        throw new Exception("Plaid connection failed: " + err);
    }

    /** Create link_token */
    public String createLinkToken(String userId) throws Exception {
        LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser().clientUserId(userId);
        LinkTokenCreateRequest req = new LinkTokenCreateRequest()
                .user(user)
                .clientName("Finpilot")
                .products(List.of(Products.TRANSACTIONS))
                .countryCodes(toCountryCodes(props.getCountryCodes()))
                .language("en");
        Response<LinkTokenCreateResponse> resp = plaidApi.linkTokenCreate(req).execute();
        if (resp.isSuccessful() && resp.body() != null) {
            return resp.body().getLinkToken();
        }
        String err = (resp.errorBody() != null) ? resp.errorBody().string() : "unknown error";
        throw new Exception("Plaid link_token create failed: " + err);
    }

    /** Overload that can capture institution id/name (if your request DTO provides them). */
    public PlaidPublicTokenExchangeResponse exchangePublicToken(String userId, String publicToken) throws Exception {
        // 1) Exchange
        ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest().publicToken(publicToken);
        Response<ItemPublicTokenExchangeResponse> response = plaidApi.itemPublicTokenExchange(request).execute();
        if (!response.isSuccessful() || response.body() == null) {
            String err = (response.errorBody() != null) ? response.errorBody().string() : "unknown error";
            throw new Exception("Plaid public token exchange failed: " + err);
        }

        String accessToken = response.body().getAccessToken();
        String itemId      = response.body().getItemId();

        // 2) Encrypt
        String accessTokenEnc = crypto.encrypt(accessToken);

        // 3) Upsert PlaidItem (create if not exists)
        var existing = plaidItemRepo.findByPlaidItemIdAndUserId(itemId, userId);
        PlaidItem entity = existing.orElseGet(() -> {
            PlaidItem pi = new PlaidItem();
            pi.setUserId(userId);
            pi.setItemId(itemId);
            return pi;
        });
        entity.setAccessTokenEnc(accessTokenEnc);

        // 4) Fetch institution metadata and store it
        try {
            // items/get → institution_id
            ItemGetRequest igReq = new ItemGetRequest().accessToken(accessToken);
            Response<ItemGetResponse> igResp = plaidApi.itemGet(igReq).execute();
            if (igResp.isSuccessful() && igResp.body() != null && igResp.body().getItem() != null) {
                String instId = igResp.body().getItem().getInstitutionId();
                entity.setInstitutionId(instId);

                // institutions/get_by_id → human name
                if (instId != null && !instId.isBlank()) {
                    InstitutionsGetByIdRequest instReq = new InstitutionsGetByIdRequest()
                            .institutionId(instId)
                            .countryCodes(toCountryCodes(props.getCountryCodes()));
                    Response<InstitutionsGetByIdResponse> instResp = plaidApi.institutionsGetById(instReq).execute();
                    if (instResp.isSuccessful()
                            && instResp.body() != null
                            && instResp.body().getInstitution() != null) {
                        entity.setInstitutionName(instResp.body().getInstitution().getName());
                    }
                }
            }
        } catch (Exception swallow) {
            // non-fatal: leave institution fields null if Plaid lookups fail
        }

        plaidItemRepo.save(entity);

        return new PlaidPublicTokenExchangeResponse(
                true,
                itemId,
                "Stored encrypted access token for item_id " + itemId
        );
    }

    /** Simple /accounts/get demo */
    public List<String> getAccountsForItem(String userId, UUID itemDbId) throws Exception {
        var item = plaidItemRepo.findByIdAndUserId(itemDbId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        String accessToken = crypto.decrypt(item.getAccessTokenEnc());

        AccountsGetRequest req = new AccountsGetRequest().accessToken(accessToken);
        Response<AccountsGetResponse> resp = plaidApi.accountsGet(req).execute();

        if (!resp.isSuccessful() || resp.body() == null) {
            String err = (resp.errorBody() != null) ? resp.errorBody().string() : "unknown";
            throw new Exception("accountsGet failed: " + err);
        }

        return resp.body().getAccounts().stream()
                .map(a -> {
                    String name = a.getName() != null ? a.getName() : a.getOfficialName();
                    String mask = a.getMask();
                    String subtype = (a.getSubtype() != null) ? a.getSubtype().getValue() : "";
                    return (name != null ? name : "Account")
                            + (mask != null ? " ••••" + mask : "")
                            + (subtype.isBlank() ? "" : " (" + subtype + ")");
                })
                .toList();
    }

    /** Tiny probe using /transactions/get (used earlier for eyeballing) */
    public List<Map<String, Object>> probeTransactions(String userId, UUID itemDbId, int days) throws Exception {
        var item = plaidItemRepo.findByIdAndUserId(itemDbId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        String accessToken = crypto.decrypt(item.getAccessTokenEnc());

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(Math.max(1, days));

        TransactionsGetRequest req = new TransactionsGetRequest()
                .accessToken(accessToken)
                .startDate(start)
                .endDate(end)
                .options(new TransactionsGetRequestOptions().includePersonalFinanceCategory(true));

        Response<TransactionsGetResponse> resp = plaidApi.transactionsGet(req).execute();
        if (!resp.isSuccessful() || resp.body() == null) {
            String err = (resp.errorBody() != null) ? resp.errorBody().string() : "unknown";
            throw new Exception("transactionsGet failed: " + err);
        }

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

    private boolean hasPlaidTxn(String userId, String plaidTxnId) {
        if (plaidTxnId == null || plaidTxnId.isBlank()) return false;
        return txRepo.existsByUserIdAndPlaidTransactionId(userId, plaidTxnId);
    }

    /** Cursor-based sync; persists new cursor; upserts only ADDED for now. */
    public PlaidSyncResponse transactionsSync(String userId, UUID itemDbId) throws Exception {
        var item = plaidItemRepo.findByIdAndUserId(itemDbId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        String prevCursor = item.getNextCursor(); // capture BEFORE updating (for firstSync flag)

        String accessToken = crypto.decrypt(item.getAccessTokenEnc());

        TransactionsSyncRequestOptions opts = new TransactionsSyncRequestOptions()
                .includePersonalFinanceCategory(true);

        TransactionsSyncRequest req = new TransactionsSyncRequest()
                .accessToken(accessToken)
                .cursor(prevCursor)
                .options(opts);

        Response<TransactionsSyncResponse> resp = plaidApi.transactionsSync(req).execute();
        if (!resp.isSuccessful() || resp.body() == null) {
            String err = (resp.errorBody() != null) ? resp.errorBody().string() : "unknown";
            throw new Exception("transactionsSync failed: " + err);
        }

        TransactionsSyncResponse body = resp.body();

        // Save new cursor for next time
        item.setNextCursor(body.getNextCursor());
        plaidItemRepo.save(item);

        int created = 0, updated = 0, removed = 0, skipped = 0;

        // Handle ADDED → insert if not present
        if (body.getAdded() != null) {
            for (com.plaid.client.model.Transaction pt : body.getAdded()) {
                String plaidTxnId = pt.getTransactionId();
                if (hasPlaidTxn(userId, plaidTxnId)) {
                    skipped++;
                    continue;
                }

                var tx = new com.anshdesai.finpilot.model.Transaction();
                tx.setUserId(userId);
                tx.setDate(pt.getDate());

                // Amount sign: make INCOME positive, everything else negative (Plaid amounts are positive for outflows)
                var pfc = pt.getPersonalFinanceCategory();
                boolean isIncome = (pfc != null && "INCOME".equalsIgnoreCase(pfc.getPrimary()));
                BigDecimal amt = BigDecimal.valueOf(pt.getAmount());
                tx.setAmount(isIncome ? amt : amt.negate());

                tx.setMerchant(pt.getName() != null ? pt.getName() : "");
                if (pfc != null && pfc.getPrimary() != null) {
                    tx.setCategory(pfc.getPrimary()); // legacy text field, you already map to your Category table elsewhere
                }
                tx.setPlaidTransactionId(plaidTxnId);
                tx.setPlaidAccountId(pt.getAccountId());
                tx.setPending(Boolean.TRUE.equals(pt.getPending()));
                tx.setPlaidItem(item);

                txRepo.save(tx);
                created++;
            }
        }

        int modCount = body.getModified() == null ? 0 : body.getModified().size();
        int remCount = body.getRemoved() == null ? 0 : body.getRemoved().size();

        boolean firstSync = (prevCursor == null || prevCursor.isBlank());

        return new PlaidSyncResponse(
                created,
                modCount,
                remCount,
                firstSync
        );
    }

    /** Summaries for dropdowns etc. */
    public List<PlaidItemSummary> listItems(String userId) {
        // If your repo has a custom finder, use it; otherwise filter in memory.
        return plaidItemRepo.findAll().stream()
                .filter(pi -> userId.equals(pi.getUserId()))
                .map(pi -> new PlaidItemSummary(
                        pi.getId(),
                        pi.getPlaidItemId(),
                        pi.getInstitutionId(),
                        pi.getInstitutionName(),
                        pi.getCreatedAt()
                ))
                .sorted((a, b) -> {
                    // newest first (null-safe)
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();
    }
}