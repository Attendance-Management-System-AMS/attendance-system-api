-- ============================================================
-- AMS MONOLITH: V2__seed_master_data.sql
-- Seed Departments, Positions, Shifts, and Leave Types
-- ============================================================

-- 1. DEPARTMENTS
INSERT INTO departments (name, description, status) VALUES 
('Ban Giám Đốc', 'Ban điều hành công ty', 'ACTIVE'),
('Phòng Nhân Sự', 'Quản lý nhân sự và tuyển dụng', 'ACTIVE'),
('Phòng IT', 'Phát triển phần mềm và hạ tầng công nghệ', 'ACTIVE'),
('Phòng Kinh Doanh', 'Kinh doanh và phát triển thị trường', 'ACTIVE'),
('Phòng Kế Toán', 'Quản lý tài chính và kế toán', 'ACTIVE');

-- 2. POSITIONS (Assuming IDs 1-5 for departments above)
-- Ban Giám Đốc
INSERT INTO positions (name, department_id, level) VALUES ('Tổng Giám Đốc', 1, 'LEVEL_1');
INSERT INTO positions (name, department_id, level) VALUES ('Phó Tổng Giám Đốc', 1, 'LEVEL_2');

-- HR
INSERT INTO positions (name, department_id, level) VALUES ('Trưởng Phòng Nhân Sự', 2, 'LEVEL_3');
INSERT INTO positions (name, department_id, level) VALUES ('Chuyên Viên Nhân Sự', 2, 'LEVEL_4');

-- IT
INSERT INTO positions (name, department_id, level) VALUES ('Trưởng Phòng IT', 3, 'LEVEL_3');
INSERT INTO positions (name, department_id, level) VALUES ('Lập Trình Viên Senior', 3, 'LEVEL_4');
INSERT INTO positions (name, department_id, level) VALUES ('Lập Trình Viên Junior', 3, 'LEVEL_5');

-- Kinh Doanh
INSERT INTO positions (name, department_id, level) VALUES ('Trưởng Phòng Kinh Doanh', 4, 'LEVEL_3');
INSERT INTO positions (name, department_id, level) VALUES ('Nhân Viên Kinh Doanh', 4, 'LEVEL_4');

-- Kế Toán
INSERT INTO positions (name, department_id, level) VALUES ('Kế Toán Trưởng', 5, 'LEVEL_3');
INSERT INTO positions (name, department_id, level) VALUES ('Nhân Viên Kế Toán', 5, 'LEVEL_4');

-- 3. SHIFTS
INSERT INTO shifts (name, start_time, end_time, break_start, break_end, grace_period) VALUES 
('Ca Hành Chính', '08:00:00', '17:00:00', '12:00:00', '13:00:00', 15),
('Ca Sáng', '06:00:00', '14:00:00', '10:00:00', '10:30:00', 10),
('Ca Chiều', '14:00:00', '22:00:00', '18:00:00', '18:30:00', 10);

-- 4. LEAVE TYPES
INSERT INTO leave_types (code, name, is_paid, deduct_annual_leave, insurance_covers, is_active, description) VALUES 
('AL', 'Nghỉ phép năm', true, true, false, true, 'Nghỉ phép hưởng lương định kỳ hàng năm'),
('CL', 'Nghỉ chế độ (Ốm)', false, false, true, true, 'Nghỉ ốm đau có bảo hiểm chi trả'),
('UL', 'Nghỉ không lương', false, false, false, true, 'Nghỉ việc riêng không hưởng lương'),
('ML', 'Nghỉ thai sản', true, false, true, true, 'Nghỉ thai sản theo quy định nhà nước');

-- 5. SCHEDULE TEMPLATES
INSERT INTO schedule_templates (name, description) VALUES 
('Lịch trực văn phòng', 'Lịch làm việc chuẩn 08:00 - 17:00 từ Thứ 2 đến Thứ 6');

-- Seed template items (Assuming template_id=1, shift_id=1)
-- Monday(1) to Friday(5)
INSERT INTO schedule_template_items (template_id, day_of_week, shift_id) VALUES (1, 1, 1);
INSERT INTO schedule_template_items (template_id, day_of_week, shift_id) VALUES (1, 2, 1);
INSERT INTO schedule_template_items (template_id, day_of_week, shift_id) VALUES (1, 3, 1);
INSERT INTO schedule_template_items (template_id, day_of_week, shift_id) VALUES (1, 4, 1);
INSERT INTO schedule_template_items (template_id, day_of_week, shift_id) VALUES (1, 5, 1);
