CREATE TABLE IF NOT EXISTS plaid_items (
   id                UUID PRIMARY KEY,
   user_id           VARCHAR(255) NOT NULL,   -- same type as your other tables
   plaid_item_id     TEXT NOT NULL,           -- Plaid's item_id
   access_token_enc  TEXT NOT NULL,           -- ENCRYPTED ciphertext (never store plaintext)
   institution_id    TEXT,                    -- e.g., "ins_12345"
   institution_name  TEXT,                    -- human label for UI
   created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
   updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

/* Each user can only have one row per Plaid item_id */
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM   pg_constraint
            WHERE  conname = 'ux_plaid_items_user_item'
        ) THEN
            ALTER TABLE plaid_items
                ADD CONSTRAINT ux_plaid_items_user_item
                    UNIQUE (user_id, plaid_item_id);
        END IF;
    END$$;

/* Keep updated_at fresh on updates */
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_proc WHERE proname = 'trg_set_timestamp_plaid_items'
        ) THEN
            CREATE OR REPLACE FUNCTION trg_set_timestamp_plaid_items()
                RETURNS TRIGGER AS $f$
            BEGIN
                NEW.updated_at := NOW();
                RETURN NEW;
            END;
            $f$ LANGUAGE plpgsql;

            CREATE TRIGGER plaid_items_set_timestamp
                BEFORE UPDATE ON plaid_items
                FOR EACH ROW
            EXECUTE FUNCTION trg_set_timestamp_plaid_items();
        END IF;
    END$$;

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS plaid_account_id     TEXT,
    ADD COLUMN IF NOT EXISTS plaid_transaction_id TEXT,
    ADD COLUMN IF NOT EXISTS pending              BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS iso_currency         TEXT;

/* (Optional but harmless) a lightweight guard that currency looks like ISO-4217 */
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM   pg_constraint
            WHERE  conname = 'chk_transactions_iso_currency_len'
        ) THEN
            ALTER TABLE transactions
                ADD CONSTRAINT chk_transactions_iso_currency_len
                    CHECK (iso_currency IS NULL OR char_length(iso_currency) = 3);
        END IF;
    END$$;

/* 3.1 Prevent duplicate Plaid transactions for the same user */
CREATE UNIQUE INDEX IF NOT EXISTS ux_tx_user_plaid_txid
    ON transactions(user_id, plaid_transaction_id)
    WHERE plaid_transaction_id IS NOT NULL;

/* 3.2 Default sort path: queries like
       SELECT ... FROM transactions
       WHERE user_id = ? ORDER BY date DESC, id DESC LIMIT ? */
CREATE INDEX IF NOT EXISTS idx_tx_user_date_desc
    ON transactions(user_id, date DESC, id DESC);

/* 3.3 Case-insensitive merchant lookups (filters/search) */
CREATE INDEX IF NOT EXISTS idx_tx_user_merchant_lower
    ON transactions(user_id, lower(merchant));

/* 3.4 Heuristic dedupe when there is no Plaid ID:
       (user, amount, date) is a common first-pass probe */
CREATE INDEX IF NOT EXISTS idx_tx_user_amount_date
    ON transactions(user_id, amount, date);

