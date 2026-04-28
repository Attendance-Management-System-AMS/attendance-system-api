ALTER TABLE attendances
    ADD COLUMN IF NOT EXISTS late_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS early_leave_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS worked_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS expected_minutes INTEGER;
