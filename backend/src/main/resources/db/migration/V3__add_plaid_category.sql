ALTER TABLE transactions
    ADD COLUMN plaid_category VARCHAR(255),
    ADD COLUMN plaid_detailed_category VARCHAR(255);