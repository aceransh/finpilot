-- Add user_id to transactions
ALTER TABLE transactions
    ADD COLUMN user_id VARCHAR(128) NOT NULL DEFAULT 'dev-user';

-- Add user_id to categories
ALTER TABLE categories
    ADD COLUMN user_id VARCHAR(128) NOT NULL DEFAULT 'dev-user';

-- Add user_id to rules
ALTER TABLE rules
    ADD COLUMN user_id VARCHAR(128) NOT NULL DEFAULT 'dev-user';

-- Indexes to make lookups fast
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_rules_user_id ON rules(user_id);