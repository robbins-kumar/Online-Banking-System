CREATE DATABASE IF NOT EXISTS online_banking;
USE online_banking;

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(15),
    password_hash VARCHAR(200) NOT NULL,
    role          ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    account_no BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    type       ENUM('SAVINGS','CURRENT') NOT NULL DEFAULT 'SAVINGS',
    balance    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status     ENUM('ACTIVE','FROZEN') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_acc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) AUTO_INCREMENT = 1000100001;

CREATE TABLE IF NOT EXISTS transactions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_no     BIGINT NOT NULL,
    type           ENUM('DEPOSIT','WITHDRAW','TRANSFER_OUT','TRANSFER_IN') NOT NULL,
    amount         DECIMAL(15,2) NOT NULL,
    other_account  BIGINT NULL,
    balance_after  DECIMAL(15,2) NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_acc FOREIGN KEY (account_no) REFERENCES accounts(account_no) ON DELETE CASCADE,
    INDEX idx_tx_acc (account_no)
);
-- The default admin (admin@bank.com / admin123) is created automatically on first app start.
