
-- V1: Initial SmartFin database schema

-- 1. Users
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bank accounts
CREATE TABLE bank_accounts (
                               id UUID PRIMARY KEY,
                               account_number VARCHAR(20) NOT NULL UNIQUE,
                               user_id UUID,
                               account_type VARCHAR(20) NOT NULL,
                               balance NUMERIC(19,2) NOT NULL DEFAULT 0.00,
                               currency VARCHAR(3) NOT NULL DEFAULT 'INR',
                               status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               opening_balance NUMERIC(19,2) NOT NULL,

                               CONSTRAINT bank_accounts_user_id_fkey
                                   FOREIGN KEY (user_id) REFERENCES users(id),

                               CONSTRAINT chk_account_balance
                                   CHECK (balance >= 0),

                               CONSTRAINT chk_account_status
                                   CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED')),

                               CONSTRAINT chk_account_type
                                   CHECK (account_type IN ('SAVINGS', 'CURRENT'))
);

-- 3. Transactions
CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              reference_number VARCHAR(50) NOT NULL UNIQUE,
                              initiated_by UUID,
                              transaction_type VARCHAR(20) NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              idempotency_key VARCHAR(100),
                              description VARCHAR(255),
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT transactions_initiated_by_fkey
                                  FOREIGN KEY (initiated_by) REFERENCES users(id),

                              CONSTRAINT uk_transactions_idempotency_key
                                  UNIQUE (idempotency_key),

                              CONSTRAINT chk_transaction_status
                                  CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),

                              CONSTRAINT chk_transaction_type
                                  CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER'))
);

-- 4. Ledger entries
CREATE TABLE ledger_entries (
                                id UUID PRIMARY KEY,
                                transaction_id UUID NOT NULL,
                                account_id UUID NOT NULL,
                                entry_type VARCHAR(10) NOT NULL,
                                amount NUMERIC(19,2) NOT NULL,
                                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT ledger_entries_transaction_id_fkey
                                    FOREIGN KEY (transaction_id) REFERENCES transactions(id),

                                CONSTRAINT ledger_entries_account_id_fkey
                                    FOREIGN KEY (account_id) REFERENCES bank_accounts(id),

                                CONSTRAINT chk_entry_amount
                                    CHECK (amount > 0),

                                CONSTRAINT chk_entry_type
                                    CHECK (entry_type IN ('DEBIT', 'CREDIT'))
);

-- 5. Additional indexes
CREATE INDEX idx_transactions_created_at
    ON transactions(created_at);
