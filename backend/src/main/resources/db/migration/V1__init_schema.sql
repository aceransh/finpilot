-- Create ENUM type for match_type
-- (If this type already exists from a previous run, the DROP SCHEMA step below will clear it, so this is safe)
CREATE TYPE match_type_enum AS ENUM ('CONTAINS', 'EXACT', 'STARTS_WITH', 'REGEX');

-- Create users table
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create plaid_items table
CREATE TABLE plaid_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id UUID NOT NULL,
                             access_token VARCHAR(255) NOT NULL,
                             item_id VARCHAR(255) NOT NULL,
                             status VARCHAR(50) NOT NULL,
                             CONSTRAINT fk_plaid_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create accounts table
CREATE TABLE accounts (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          plaid_item_id UUID NOT NULL,
                          plaid_account_id VARCHAR(255) NOT NULL UNIQUE,
                          name VARCHAR(255) NOT NULL,
                          type VARCHAR(50) NOT NULL,
                          balance DECIMAL(19, 2) NOT NULL,
                          CONSTRAINT fk_accounts_plaid_item FOREIGN KEY (plaid_item_id) REFERENCES plaid_items(id) ON DELETE CASCADE
);

-- Create categories table (FIXED: NOW USES UUID)
CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID,
                            name VARCHAR(255) NOT NULL,
                            color_hex VARCHAR(7) NOT NULL,
                            type VARCHAR(50) NOT NULL DEFAULT 'EXPENSE', -- Added default type column often needed by Java entity
                            CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create categorization_rules table (FIXED: FK is now UUID)
CREATE TABLE categorization_rules (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      user_id UUID NOT NULL,
                                      keyword TEXT NOT NULL,
                                      category_id UUID NOT NULL, -- Linked to UUID categories
                                      priority INTEGER NOT NULL,
                                      match_type match_type_enum NOT NULL,
                                      CONSTRAINT fk_categorization_rules_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_categorization_rules_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Create transactions table (FIXED: FK is now UUID)
CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              account_id UUID NOT NULL,
                              plaid_transaction_id VARCHAR(255) NOT NULL UNIQUE,
                              amount DECIMAL(19, 2) NOT NULL,
                              date DATE NOT NULL,
                              description TEXT,
                              category_id UUID, -- Linked to UUID categories
                              CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
                              CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);