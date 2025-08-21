ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS plaid_item_id UUID;

CREATE INDEX IF NOT EXISTS idx_tx_plaid_item
    ON transactions(plaid_item_id);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM   information_schema.table_constraints
            WHERE  table_name = 'transactions'
              AND  constraint_type = 'FOREIGN KEY'
              AND  constraint_name = 'fk_tx_plaid_item'
        ) THEN
            ALTER TABLE transactions
                ADD CONSTRAINT fk_tx_plaid_item
                    FOREIGN KEY (plaid_item_id)
                        REFERENCES plaid_items(id)
                        ON UPDATE CASCADE
                        ON DELETE SET NULL;  -- if an item is removed, keep the txn but drop the link
        END IF;
    END$$;