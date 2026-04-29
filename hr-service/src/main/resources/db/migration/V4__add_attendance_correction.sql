-- V4: Thêm loại đơn "Giải trình công" và cột bổ sung cho leave_requests

-- 1. Thêm loại đơn Giải trình công (Attendance Correction)
INSERT INTO leave_types (code, name, is_paid, deduct_annual_leave, insurance_covers, is_active, description)
VALUES ('AC', 'Giải trình công', false, false, false, true, 'Đơn giải trình bổ sung giờ chấm công bị thiếu')
ON CONFLICT (code) DO NOTHING;

-- 2. Thêm cột giờ vào/ra bổ sung cho đơn giải trình công
ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS corrected_check_in TIME;
ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS corrected_check_out TIME;
