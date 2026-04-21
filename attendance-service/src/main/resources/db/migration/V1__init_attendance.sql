-- ============================================================
-- ATTENDANCE SERVICE: V1__init_attendance.sql
-- Owns attendance, logs, shifts, schedules, holidays and templates.
-- employee_id is a logical reference to hr-service.
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
    id                   BIGSERIAL       PRIMARY KEY,
    employee_id          BIGINT          NOT NULL,
    check_in_time        TIMESTAMP,
    check_out_time       TIMESTAMP,
    work_date            DATE            NOT NULL,
    status               VARCHAR(255),
    late_minutes         INTEGER,
    early_leave_minutes  INTEGER,
    worked_minutes       INTEGER,
    expected_minutes     INTEGER,
    created_at           TIMESTAMP       DEFAULT now()
);

CREATE TABLE attendance_logs (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL,
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

CREATE UNIQUE INDEX uq_attendances_employee_work_date ON attendances(employee_id, work_date);
CREATE INDEX idx_attendance_logs_employee_log_time ON attendance_logs(employee_id, log_time);
CREATE INDEX idx_employee_schedules_employee_id ON employee_schedules(employee_id);
CREATE INDEX idx_employee_schedules_day_active ON employee_schedules(day_of_week, is_active);

INSERT INTO shifts (name, start_time, end_time, break_start, break_end, grace_period) VALUES
('Ca Hành Chính', '08:00:00', '17:00:00', '12:00:00', '13:00:00', 15),
('Ca Sáng', '06:00:00', '14:00:00', '10:00:00', '10:30:00', 10),
('Ca Chiều', '14:00:00', '22:00:00', '18:00:00', '18:30:00', 10);

INSERT INTO schedule_templates (name, description) VALUES
('Lịch trực văn phòng', 'Lịch làm việc chuẩn 08:00 - 17:00 từ Thứ 2 đến Thứ 6');

INSERT INTO schedule_template_items (template_id, day_of_week, shift_id) VALUES
(1, 1, 1),
(1, 2, 1),
(1, 3, 1),
(1, 4, 1),
(1, 5, 1);
