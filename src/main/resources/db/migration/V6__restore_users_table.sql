 -- Удаляем существующую таблицу
DROP TABLE IF EXISTS users CASCADE;

-- Создаем таблицу users с нужными полями
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL
);

-- Индекс для email
CREATE INDEX idx_users_email ON users(email); 