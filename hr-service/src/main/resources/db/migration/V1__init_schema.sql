-- ============================================================
-- hr-service: V1__init_schema.sql
-- Database: hr_service_db
-- ============================================================

CREATE TABLE departments (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(120)    NOT NULL,
    description     VARCHAR(1000),
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE TABLE positions (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(120)    NOT NULL,
    department_id   BIGINT          NOT NULL REFERENCES departments(id),
    level           VARCHAR(50)     NOT NULL,
    parent_id       BIGINT          REFERENCES positions(id)
);

CREATE TABLE employees (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          UNIQUE,
    employee_code   VARCHAR(50)     UNIQUE NOT NULL,
    full_name       VARCHAR(180)    NOT NULL,
    gender          VARCHAR(20),
    email           VARCHAR(190)    UNIQUE,
    department_id   BIGINT          REFERENCES departments(id),
    position_id     BIGINT          REFERENCES positions(id),
    manager_id      BIGINT          REFERENCES employees(id),
    status          VARCHAR(40),
    biometric_hash  VARCHAR(512),
    join_date       DATE,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE TABLE leave_requests (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id),
    leave_type      VARCHAR(50)     NOT NULL,
    from_date       DATE            NOT NULL,
    to_date         DATE            NOT NULL,
    total_days      DOUBLE PRECISION NOT NULL,
    reason          VARCHAR(500),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    approved_by     BIGINT          REFERENCES employees(id),
    created_at      TIMESTAMP       DEFAULT now()
);
