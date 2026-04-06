-- ============================================================
-- attendance-service: V3__create_attendance_logs.sql
-- Thêm bảng lưu trữ log quét thẻ / chấm công thô
-- ============================================================

CREATE TABLE attendance_logs (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL,
    log_time        TIMESTAMP       NOT NULL,
    device_id       VARCHAR(100),
    log_type        VARCHAR(50),    -- IN, OUT
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);
