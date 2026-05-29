--liquibase formatted sql
--changeset chaika:1

CREATE TABLE user_credentials (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

--Indexes
CREATE INDEX idx_user_credentials_email ON user_credentials (email);