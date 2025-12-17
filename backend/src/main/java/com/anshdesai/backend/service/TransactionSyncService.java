package com.anshdesai.backend.service;

import com.anshdesai.backend.model.Account;
import com.anshdesai.backend.model.PlaidItem;
import com.anshdesai.backend.model.Transaction;
import com.anshdesai.backend.model.User;
import com.anshdesai.backend.repository.AccountRepository;
import com.anshdesai.backend.repository.PlaidItemRepository;
import com.anshdesai.backend.repository.TransactionRepository;
import com.plaid.client.model.TransactionsGetResponse;
import com.plaid.client.model.TransactionsGetRequestOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionSyncService {

    private final PlaidService plaidService;
    private final PlaidItemRepository plaidItemRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public int syncTransactions(User user) {
        // Find all PlaidItems for the user
        List<PlaidItem> plaidItems = plaidItemRepository.findByUser(user);

        int totalSaved = 0;

        // Calculate date range (last 30 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        // For each PlaidItem, fetch and save transactions
        for (PlaidItem plaidItem : plaidItems) {
            try {
                // Create options to include personal finance categories
                TransactionsGetRequestOptions options = new TransactionsGetRequestOptions()
                        .includePersonalFinanceCategory(true);

                // Get transactions from Plaid
                TransactionsGetResponse response = plaidService.getTransactions(
                        plaidItem.getAccessToken(),
                        startDate,
                        endDate,
                        options
                );

                if (response == null) {
                    continue;
                }

                // Step A: Extract accounts list from response
                if (response.getAccounts() != null) {
                    for (com.plaid.client.model.AccountBase plaidAccount : response.getAccounts()) {
                        createOrUpdateAccount(plaidItem, plaidAccount);
                    }
                }

                // Get all accounts for this PlaidItem and create a map by plaid_account_id
                List<Account> accounts = accountRepository.findByPlaidItem(plaidItem);

                Map<String, Account> accountMap = accounts.stream()
                        .collect(Collectors.toMap(Account::getPlaidAccountId, acc -> acc));

                // Step C: Process transactions (existing logic)
                if (response.getTransactions() == null) {
                    continue;
                }

                // Process each transaction
                for (com.plaid.client.model.Transaction plaidTxn : response.getTransactions()) {
                    // Check if transaction already exists (deduplication)
                    if (transactionRepository.findByPlaidTransactionId(plaidTxn.getTransactionId()).isPresent()) {
                        continue; // Skip duplicate
                    }

                    // Find the account for this transaction
                    Account account = accountMap.get(plaidTxn.getAccountId());
                    if (account == null) {
                        continue; // Skip if account not found
                    }

                    // Map Plaid transaction to our Transaction entity
                    // Plaid date is a LocalDate object
                    LocalDate transactionDate = plaidTxn.getDate() != null 
                            ? plaidTxn.getDate() 
                            : LocalDate.now();

                    // Extract Plaid categories (check for nulls)
                    String plaidCategory = null;
                    String plaidDetailedCategory = null;
                    
                    if (plaidTxn.getPersonalFinanceCategory() != null) {
                        if (plaidTxn.getPersonalFinanceCategory().getPrimary() != null) {
                            plaidCategory = plaidTxn.getPersonalFinanceCategory().getPrimary();
                        }
                        if (plaidTxn.getPersonalFinanceCategory().getDetailed() != null) {
                            plaidDetailedCategory = plaidTxn.getPersonalFinanceCategory().getDetailed();
                        }
                    }

                    Transaction transaction = Transaction.builder()
                            .account(account)
                            .plaidTransactionId(plaidTxn.getTransactionId())
                            .amount(BigDecimal.valueOf(plaidTxn.getAmount()))
                            .date(transactionDate)
                            .description(plaidTxn.getName())
                            .plaidCategory(plaidCategory)
                            .plaidDetailedCategory(plaidDetailedCategory)
                            .category(null) // Will be set by categorization rules later
                            .build();

                    transactionRepository.save(transaction);
                    totalSaved++;
                }
            } catch (Exception e) {
                // Log error but continue with other items
                System.err.println("Error syncing transactions for PlaidItem " + plaidItem.getId() + ": " + e.getMessage());
            }
        }

        return totalSaved;
    }

    /**
     * Helper method to create or update an Account entity
     */
    private void createOrUpdateAccount(PlaidItem plaidItem, com.plaid.client.model.AccountBase plaidAccount) {
        // Check if account already exists by plaidAccountId
        String plaidAccountId = plaidAccount.getAccountId();
        Account existingAccount = accountRepository.findByPlaidAccountId(plaidAccountId)
                .orElse(null);

        if (existingAccount != null) {
            // Update balance if account exists
            if (plaidAccount.getBalances() != null) {
                Double currentBalance = plaidAccount.getBalances().getCurrent();
                if (currentBalance != null) {
                    existingAccount.setBalance(BigDecimal.valueOf(currentBalance));
                    accountRepository.save(existingAccount);
                }
            }
        } else {
            // Create new Account entity
            BigDecimal balance = BigDecimal.ZERO;
            if (plaidAccount.getBalances() != null) {
                Double currentBalance = plaidAccount.getBalances().getCurrent();
                if (currentBalance != null) {
                    balance = BigDecimal.valueOf(currentBalance);
                }
            }

            Account newAccount = Account.builder()
                    .plaidItem(plaidItem)
                    .plaidAccountId(plaidAccountId)
                    .name(plaidAccount.getName() != null ? plaidAccount.getName() : "Unknown Account")
                    .type(plaidAccount.getType() != null ? plaidAccount.getType().toString() : "unknown")
                    .balance(balance)
                    .build();

            accountRepository.save(newAccount);
        }
    }
}

