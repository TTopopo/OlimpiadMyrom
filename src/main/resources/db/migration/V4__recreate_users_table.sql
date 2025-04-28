-- Удаляем существующую таблицу
DROP TABLE IF EXISTS users CASCADE;

-- Создаем таблицу users заново
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    full_name VARCHAR(255) NOT NULL
);

-- Создаем индекс для email
CREATE INDEX idx_users_email ON users(email); 