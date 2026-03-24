-- ============================================================
-- attendance-service: V1__init_schema.sql
-- Database: attendance_service_db
-- ============================================================

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
    employee_id     BIGINT          NOT NULL,
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
    employee_id     BIGINT          NOT NULL,
    check_in_time   TIMESTAMP,
    check_out_time  TIMESTAMP,
    work_date       DATE            NOT NULL,
    status          VARCHAR(255),
    created_at      TIMESTAMP       DEFAULT now()
);
