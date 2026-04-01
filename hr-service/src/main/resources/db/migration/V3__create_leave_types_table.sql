-- ============================================================
-- hr-service: V3__create_leave_types_table.sql
-- Add leave_types table and migrate existing leave_requests
-- ============================================================

-- Create leave_types table
CREATE TABLE leave_types (
    id                      BIGSERIAL       PRIMARY KEY,
    code                    VARCHAR(10)     NOT NULL UNIQUE,
    name                    VARCHAR(100)    NOT NULL,
    is_paid                 BOOLEAN         NOT NULL DEFAULT TRUE,
    deduct_annual_leave     BOOLEAN         NOT NULL DEFAULT FALSE,
    insurance_covers        BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    description             VARCHAR(500)
);

-- Add leave_type_id column to leave_requests
ALTER TABLE leave_requests
ADD COLUMN leave_type_id BIGINT REFERENCES leave_types(id);

-- Insert leave types with the provided specifications
INSERT INTO leave_types (code, name, is_paid, deduct_annual_leave, insurance_covers, is_active, description) VALUES
('AL', 'Nghỉ phép năm', TRUE, TRUE, FALSE, TRUE, 'Ngày nghỉ phép hằng năm'),
('PH', 'Nghỉ lễ/Tết', TRUE, FALSE, FALSE, TRUE, 'Ngày lễ và tết nguyên đán'),
('SL', 'Nghỉ ốm', FALSE, FALSE, TRUE, TRUE, 'Ngày nghỉ do bệnh tật'),
('ML', 'Nghỉ thai sản', FALSE, FALSE, TRUE, TRUE, 'Ngày nghỉ thai sản'),
('UL', 'Nghỉ không lương', FALSE, FALSE, FALSE, TRUE, 'Ngày nghỉ không hưởng lương'),
('BT', 'Đi công tác', TRUE, FALSE, FALSE, TRUE, 'Ngày đi công tác');

-- Create indexes for better query performance
CREATE INDEX idx_leave_types_code ON leave_types(code);
CREATE INDEX idx_leave_types_is_active ON leave_types(is_active);
CREATE INDEX idx_leave_requests_leave_type_id ON leave_requests(leave_type_id);
