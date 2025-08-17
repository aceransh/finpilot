-- Ensure unique names so re-seeding won't duplicate
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = 'public' AND indexname = 'ux_categories_name'
    ) THEN
        CREATE UNIQUE INDEX ux_categories_name ON categories (lower(name));
    END IF;
END $$;

-- Insert 16 Plaid primary categories (type guessed; you can tweak)
INSERT INTO categories (id, name, type, color)
VALUES
    (gen_random_uuid(), 'Bank fees',              'EXPENSE', '#8E24AA'),
    (gen_random_uuid(), 'Entertainment',          'EXPENSE', '#FB8C00'),
    (gen_random_uuid(), 'Food & drink',           'EXPENSE', '#E53935'),
    (gen_random_uuid(), 'General merchandise',    'EXPENSE', '#3949AB'),
    (gen_random_uuid(), 'General services',       'EXPENSE', '#6D4C41'),
    (gen_random_uuid(), 'Government & non-profit','EXPENSE', '#5E35B1'),
    (gen_random_uuid(), 'Home improvement',       'EXPENSE', '#1E88E5'),
    (gen_random_uuid(), 'Income',                 'INCOME',  '#00897B'),
    (gen_random_uuid(), 'Loan payments',          'EXPENSE', '#546E7A'),
    (gen_random_uuid(), 'Medical',                'EXPENSE', '#00838F'),
    (gen_random_uuid(), 'Personal care',          'EXPENSE', '#D81B60'),
    (gen_random_uuid(), 'Rent & utilities',       'EXPENSE', '#43A047'),
    (gen_random_uuid(), 'Transfer in',            'INCOME',  '#26A69A'),
    (gen_random_uuid(), 'Transfer out',           'EXPENSE', '#26C6DA'),
    (gen_random_uuid(), 'Transportation',         'EXPENSE', '#7CB342'),
    (gen_random_uuid(), 'Travel',                 'EXPENSE', '#F4511E')
ON CONFLICT (lower(name)) DO NOTHING;