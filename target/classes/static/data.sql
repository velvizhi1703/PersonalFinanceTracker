CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    cash_in_hand DECIMAL(10,2) DEFAULT 0,
    num_transactions INT DEFAULT 0,
    enabled BOOLEAN DEFAULT FALSE,
    status ENUM('Enabled', 'Disabled') NOT NULL DEFAULT 'Enabled',
    total_expense DECIMAL(10,2) DEFAULT 0,
    total_income DECIMAL(10,2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    category VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    type ENUM('CREDIT', 'DEBIT') NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    user_id BIGINT NOT NULL,
    remaining DECIMAL(10,2) NOT NULL,
    spent DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO users (id, email, password, name, cash_in_hand, num_transactions, enabled) VALUES
(1, 'keerthi@example.com','$2a$10$45XeXhEy8f1OdyXU9QpcB.TvSaCSLZuBboBLXZ.HKvtVUQuLw6a7G', 'keerthi', -650, 13, TRUE),
(2, 'vel@example.com', '$2a$10$i7JKPtavIWcgBwi5/6bXE.o6xdAXhkesU4LUqQkjr8URIW0ujOl/e', 'vel', 0, 0, TRUE),
(3, 'sri@example.com', '$2a$10$xufq5xwaKIy7HwOU/kzAh.tOLPi14R3WSP19S64F6FjGpkPe3xTLG', 'sri', 0, 0, TRUE),
(4, 'kala@example.com', '$2a$10$XMA1iRKFIWhgiUVly0F04.POldLj7XRH8xJ6QtvrOYG./iIhkBV8W', 'kala', 0, 0, TRUE);

INSERT INTO user_roles (user_id, role) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN'),
(3, 'ROLE_USER'),
(4, 'ROLE_USER');

INSERT INTO transactions (id, amount, category, date, type, user_id) VALUES
(1, 1500.00, 'Salary', '2024-03-01', 'CREDIT', 1),
(2, 200.00, 'Groceries', '2024-03-02', 'DEBIT', 1),
(3, 50.00, 'Transport', '2024-03-03', 'DEBIT', 1),
(4, 500.00, 'Freelance Work', '2024-03-04', 'CREDIT', 1),
(5, 300.00, 'Dining', '2024-03-05', 'DEBIT', 1),
(6, 100.00, 'Utilities', '2024-03-06', 'DEBIT', 1),
(7, 250.00, 'Shopping', '2024-03-07', 'DEBIT', 1),
(8, 150.00, 'Gym Membership', '2024-03-09', 'DEBIT', 1);
(9, 1800.00, 'Salary', '2024-03-01', 'CREDIT', 3),
(10, 220.00, 'Groceries', '2024-03-02', 'DEBIT', 3),
(11, 60.00, 'Transport', '2024-03-03', 'DEBIT', 3),
(12, 550.00, 'Freelance Work', '2024-03-04', 'CREDIT', 3),
(13, 320.00, 'Dining', '2024-03-05', 'DEBIT', 3),
(14, 1700.00, 'Salary', '2024-03-01', 'CREDIT', 4),
(15, 210.00, 'Groceries', '2024-03-02', 'DEBIT', 4),
(16, 55.00, 'Transport', '2024-03-03', 'DEBIT', 4),
(17, 600.00, 'Freelance Work', '2024-03-04', 'CREDIT', 4),
(18, 280.00, 'Dining', '2024-03-05', 'DEBIT', 4);


INSERT INTO budgets (id, user_id, totalIncome, totalExpense, cashInHand) VALUES
(1, 1, 2000.00, 2650.00, -650),
(2, 2, 3000.00, 2000.00, 1000);







