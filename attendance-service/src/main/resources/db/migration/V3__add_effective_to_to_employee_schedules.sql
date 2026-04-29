ALTER TABLE employee_schedules
    ADD COLUMN IF NOT EXISTS effective_to DATE;

CREATE INDEX IF NOT EXISTS idx_employee_schedules_effective_range
    ON employee_schedules (employee_id, is_active, effective_from, effective_to);
