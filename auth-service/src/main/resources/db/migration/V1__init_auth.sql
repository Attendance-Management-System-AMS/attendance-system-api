-- ============================================================
-- AUTH SERVICE: V1__init_auth.sql
-- Owns users, roles, user-role mapping, and token blacklist.
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

CREATE TABLE token_blacklist (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    token_jti       VARCHAR(255)    NOT NULL UNIQUE,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_token_blacklist_user_id ON token_blacklist(user_id);
CREATE INDEX idx_token_blacklist_expires_at ON token_blacklist(expires_at);

INSERT INTO roles (role_name, description) VALUES
('ROLE_EMPLOYEE', 'Nhân viên thông thường'),
('ROLE_MANAGER', 'Quản lý phòng ban'),
('ROLE_HR', 'Nhân sự và vận hành chấm công'),
('ROLE_ADMIN', 'Quản trị và kiểm toán hệ thống');
