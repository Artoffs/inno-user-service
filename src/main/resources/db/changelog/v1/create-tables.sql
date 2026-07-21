CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100),
    birth_date DATE,
    email VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS payment_cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    number VARCHAR(20) NOT NULL,
    holder VARCHAR(100) NOT NULL,
    expiration_date DATE NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_cards_user FOREIGN KEY (user_id) REFERENCES users(id)
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_birth_date ON users(birth_date);

CREATE INDEX IF NOT EXISTS idx_payment_cards_user_id ON payment_cards(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_cards_active ON payment_cards(active);
CREATE INDEX IF NOT EXISTS idx_payment_cards_expiration_date ON payment_cards(expiration_date);
CREATE INDEX IF NOT EXISTS idx_payment_cards_number ON payment_cards(number);
