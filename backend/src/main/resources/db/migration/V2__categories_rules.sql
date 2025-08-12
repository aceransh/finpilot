-- Enable UUID generation via pgcrypto (Postgres)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name       TEXT NOT NULL,
  type       TEXT NOT NULL CHECK (type IN ('EXPENSE','INCOME')),
  color      TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Rules table
CREATE TABLE IF NOT EXISTS rules (
     id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     pattern     TEXT NOT NULL,                           -- e.g. "harris teeter" or "^starbucks\s+#\d+$"
     match_type  TEXT NOT NULL CHECK (match_type IN ('CONTAINS','REGEX')),
     category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
     priority    INT  NOT NULL DEFAULT 100,               -- lower = higher priority
     enabled     BOOLEAN NOT NULL DEFAULT TRUE,
     created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
     updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Add category linkage to existing transactions (non-breaking)
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS category_id     UUID NULL REFERENCES categories(id),
    ADD COLUMN IF NOT EXISTS category_locked BOOLEAN NOT NULL DEFAULT FALSE;

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_rules_priority ON rules(priority);
CREATE INDEX IF NOT EXISTS idx_rules_enabled  ON rules(enabled);
CREATE INDEX IF NOT EXISTS idx_tx_category_id ON transactions(category_id);