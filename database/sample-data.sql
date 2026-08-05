USE bank_management;

-- Clear existing data
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM customers;

-- Seed customers (Passwords are hashed 'password123')
-- BCrypt hash for 'password123': $2a$10$Mk7OzTCzmIWwsBnoSaKyZerQ6wGzXTHl50sma7sMrDQdkSn6rrbri
INSERT INTO customers (customer_id, full_name, email, phone, address, date_of_birth, password_hash, created_at)
VALUES 
(1, 'John Doe', 'john.doe@example.com', '1234567890', '123 Main St, Springfield', '1990-05-15', '$2a$10$Mk7OzTCzmIWwsBnoSaKyZerQ6wGzXTHl50sma7sMrDQdkSn6rrbri', NOW() - INTERVAL 10 DAY),
(2, 'Jane Smith', 'jane.smith@example.com', '0987654321', '456 Oak Ave, Metropolis', '1992-10-20', '$2a$10$Mk7OzTCzmIWwsBnoSaKyZerQ6wGzXTHl50sma7sMrDQdkSn6rrbri', NOW() - INTERVAL 8 DAY);

-- Seed accounts
INSERT INTO accounts (account_id, account_number, customer_id, account_type, balance, status, created_at)
VALUES 
(1, 'SAV-1000000001', 1, 'SAVINGS', 1500.00, 'ACTIVE', NOW() - INTERVAL 9 DAY),
(2, 'CUR-1000000002', 1, 'CURRENT', 5000.00, 'ACTIVE', NOW() - INTERVAL 9 DAY),
(3, 'SAV-1000000003', 2, 'SAVINGS', 2500.00, 'ACTIVE', NOW() - INTERVAL 7 DAY);

-- Seed transactions
INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, reference_number, transaction_date, status)
VALUES 
(1, 1, 'DEPOSIT', 1000.00, 'Initial Deposit', 'REF1000000001', NOW() - INTERVAL 9 DAY, 'SUCCESS'),
(2, 1, 'DEPOSIT', 500.00, 'Salary Credit', 'REF1000000002', NOW() - INTERVAL 5 DAY, 'SUCCESS'),
(3, 2, 'DEPOSIT', 5000.00, 'Opening Balance', 'REF1000000003', NOW() - INTERVAL 9 DAY, 'SUCCESS'),
(4, 3, 'DEPOSIT', 2500.00, 'Initial Deposit', 'REF1000000004', NOW() - INTERVAL 7 DAY, 'SUCCESS');
