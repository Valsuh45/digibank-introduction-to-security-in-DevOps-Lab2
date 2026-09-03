-- Seed data for the DigiBank workshop.
--
-- This migration creates two active customers, one account for each customer, and one successful transfer
-- between the seeded accounts. The data gives local runs, integration tests, and CI smoke tests stable
-- records to query without relying on manual setup.
--
INSERT INTO customers (first_name, last_name, email, identity_number, status, created_at)
VALUES
    ('Alice', 'Nkem', 'alice.nkem@example.test', 'DB-CUST-0001', 'ACTIVE', '2026-01-15T09:00:00Z'),
    ('Benoit', 'Manga', 'benoit.manga@example.test', 'DB-CUST-0002', 'ACTIVE', '2026-01-15T09:05:00Z');

INSERT INTO bank_accounts (
    account_number,
    balance,
    currency,
    account_type,
    status,
    created_at,
    customer_id
)
VALUES
    (
        '100000000001',
        7500.00,
        'XAF',
        'CURRENT',
        'ACTIVE',
        '2026-01-15T09:30:00Z',
        (SELECT id FROM customers WHERE email = 'alice.nkem@example.test')
    ),
    (
        '100000000002',
        3500.00,
        'XAF',
        'SAVINGS',
        'ACTIVE',
        '2026-01-15T09:35:00Z',
        (SELECT id FROM customers WHERE email = 'benoit.manga@example.test')
    );

INSERT INTO transfers (
    transfer_reference,
    source_account_number,
    target_account_number,
    amount,
    status,
    execution_date,
    description
)
VALUES (
    'TRF-SEED-0001',
    '100000000001',
    '100000000002',
    500.00,
    'SUCCESS',
    '2026-01-15T10:00:00Z',
    'Workshop seed transfer'
);
