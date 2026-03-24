-- ============================================================
-- auth-service: V1__init_schema.sql
-- Database: auth_service_db
-- ============================================================

CREATE TABLE roles (
    id              BIGSERIAL    PRIMARY KEY,
    role_name       VARCHAR(255) UNIQUE NOT NULL,
    description     VARCHAR(255)
);

CREATE TABLE users (
    id              BIGSERIAL    PRIMARY KEY,
    username        VARCHAR(255) UNIQUE NOT NULL,
    password        VARCHAR(255) NOT NULL,
    email           VARCHAR(255) UNIQUE,
    is_enabled      BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMP    DEFAULT now()
);

CREATE TABLE user_roles (
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    role_id         BIGINT       NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO roles (role_name, description) VALUES ('ROLE_USER', 'Default user role');
INSERT INTO roles (role_name, description) VALUES ('ROLE_ADMIN', 'Administrator role');
