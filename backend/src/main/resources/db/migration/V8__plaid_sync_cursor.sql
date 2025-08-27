-- V8__plaid_sync_cursor.sql
ALTER TABLE plaid_items
    ADD COLUMN IF NOT EXISTS next_cursor TEXT;