CREATE TABLE departments (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(120)    NOT NULL,
    description     VARCHAR(1000),
    status          VARCHAR(20)     DEFAULT 'ACTIVE',
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
    face_embedding  TEXT,
    join_date       DATE,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_employees_user_id ON employees(user_id);
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_position_id ON employees(position_id);
CREATE INDEX idx_employees_status ON employees(status);
