-- ============================================================
-- AMS MONOLITH: V1__init_monolith.sql
-- Consolidated schema for all modules (Auth, HR, Attendance)
-- ============================================================

-- 1. AUTHENTICATION & SECURITY
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

-- 2. HR & ORGANIZATIONAL STRUCTURE
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
    user_id         BIGINT          UNIQUE REFERENCES users(id),
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

CREATE TABLE leave_types (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(10)     UNIQUE NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    is_paid         BOOLEAN         NOT NULL DEFAULT TRUE,
    deduct_annual_leave BOOLEAN     NOT NULL DEFAULT TRUE,
    insurance_covers BOOLEAN        NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    description     VARCHAR(500)
);

CREATE TABLE leave_requests (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id),
    leave_type_id   BIGINT          NOT NULL REFERENCES leave_types(id),
    from_date       DATE            NOT NULL,
    to_date         DATE            NOT NULL,
    total_days      DOUBLE PRECISION NOT NULL,
    reason          VARCHAR(500),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    approved_by     BIGINT          REFERENCES employees(id),
    created_at      TIMESTAMP       DEFAULT now()
);

-- 3. ATTENDANCE & SCHEDULING
CREATE TABLE shifts (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(120)    NOT NULL,
    start_time      TIME            NOT NULL,
    end_time        TIME            NOT NULL,
    break_start     TIME,
    break_end       TIME,
    grace_period    INTEGER,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE TABLE employee_schedules (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id),
    shift_id        BIGINT          NOT NULL REFERENCES shifts(id),
    day_of_week     INTEGER         NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    effective_from  DATE            NOT NULL
);

CREATE TABLE holidays (
    id              BIGSERIAL       PRIMARY KEY,
    holiday_name    VARCHAR(200)    NOT NULL,
    from_date       DATE            NOT NULL,
    to_date         DATE            NOT NULL,
    is_paid         BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE attendances (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id),
    check_in_time   TIMESTAMP,
    check_out_time  TIMESTAMP,
    work_date       DATE            NOT NULL,
    status          VARCHAR(255),
    created_at      TIMESTAMP       DEFAULT now()
);

CREATE TABLE attendance_logs (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id),
    log_time        TIMESTAMP       NOT NULL,
    device_id       VARCHAR(255),
    log_type        VARCHAR(20),
    created_at      TIMESTAMP       DEFAULT now()
);

CREATE TABLE schedule_templates (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255)
);

CREATE TABLE schedule_template_items (
    id              BIGSERIAL       PRIMARY KEY,
    template_id     BIGINT          NOT NULL REFERENCES schedule_templates(id) ON DELETE CASCADE,
    day_of_week     INTEGER         NOT NULL,
    shift_id        BIGINT          NOT NULL REFERENCES shifts(id)
);

-- 4. INITIAL SEEDING
INSERT INTO roles (role_name, description) VALUES ('ROLE_EMPLOYEE', 'Nhân viên thông thường');
INSERT INTO roles (role_name, description) VALUES ('ROLE_MANAGER', 'Quản lý phòng ban');
INSERT INTO roles (role_name, description) VALUES ('ROLE_HR', 'Nhân sự và vận hành chấm công');
INSERT INTO roles (role_name, description) VALUES ('ROLE_ADMIN', 'Quản trị và kiểm toán hệ thống');
