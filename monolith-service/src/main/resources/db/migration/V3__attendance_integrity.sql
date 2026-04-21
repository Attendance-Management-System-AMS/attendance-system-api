-- Prevent duplicate daily attendance summaries for the same employee.
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendances_employee_work_date
    ON attendances (employee_id, work_date);

CREATE INDEX IF NOT EXISTS idx_attendance_logs_employee_log_time
    ON attendance_logs (employee_id, log_time);
