--liquibase formatted sql

--changeset chaika:1
CREATE TABLE user_credentials (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

--Indexes
CREATE INDEX idx_user_credentials_email ON user_credentials (login);

--changeset chaika:2
ALTER TABLE user_credentials
ADD COLUMN name VARCHAR(50) NOT NULL DEFAULT 'Unknown',
ADD COLUMN surname VARCHAR(50) NOT NULL DEFAULT 'Unknown',
ADD COLUMN birth_date DATE;