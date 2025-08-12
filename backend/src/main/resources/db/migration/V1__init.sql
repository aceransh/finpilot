CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    amount NUMERIC(38,2) NOT NULL,
    merchant VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_txn_date ON transactions(date);
CREATE INDEX IF NOT EXISTS idx_txn_merchant ON transactions(lower(merchant));