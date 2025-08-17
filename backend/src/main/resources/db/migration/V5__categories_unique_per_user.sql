-- Drop the old global unique index/constraint if it exists.
-- Depending on how it was created, you may have either a constraint or an index name.
-- Try dropping both safely.

DO $$
    BEGIN
        -- If it was created as a UNIQUE INDEX named ux_categories_name
        IF EXISTS (
            SELECT 1 FROM pg_indexes
            WHERE schemaname = 'public' AND indexname = 'ux_categories_name'
        ) THEN
            EXECUTE 'DROP INDEX IF EXISTS ux_categories_name';
        END IF;
    EXCEPTION WHEN undefined_table THEN
    -- ignore
    END$$;

-- If it was created as a TABLE constraint with that name, drop it too.
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE table_schema = 'public'
              AND table_name = 'categories'
              AND constraint_name = 'ux_categories_name'
        ) THEN
            EXECUTE 'ALTER TABLE categories DROP CONSTRAINT ux_categories_name';
        END IF;
    EXCEPTION WHEN undefined_object THEN
    -- ignore
    END$$;

-- Create per-user unique index on (user_id, lower(name))
CREATE UNIQUE INDEX IF NOT EXISTS ux_categories_user_lower_name
    ON categories (user_id, lower(name));