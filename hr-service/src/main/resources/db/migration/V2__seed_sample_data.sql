-- ============================================================
-- HR SERVICE: V2__seed_sample_data.sql
-- Seed sample organization structure and employees.
-- ============================================================

INSERT INTO departments (id, name, description, status, created_at) VALUES
    (1, 'Ban Giám Đốc', 'Ban điều hành công ty', 'ACTIVE', now()),
    (2, 'Phòng Nhân Sự', 'Quản lý nhân sự và tuyển dụng', 'ACTIVE', now()),
    (3, 'Phòng IT', 'Phát triển phần mềm và hạ tầng công nghệ', 'ACTIVE', now()),
    (4, 'Phòng Kinh Doanh', 'Kinh doanh và phát triển thị trường', 'ACTIVE', now()),
    (5, 'Phòng Kế Toán', 'Quản lý tài chính và kế toán', 'ACTIVE', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO positions (id, name, department_id, level, parent_id) VALUES
    (1, 'Tổng Giám Đốc', 1, 'LEVEL_1', NULL),
    (2, 'Trưởng Phòng Nhân Sự', 2, 'LEVEL_3', NULL),
    (3, 'Trưởng Phòng IT', 3, 'LEVEL_3', NULL),
    (4, 'Lập Trình Viên Senior', 3, 'LEVEL_4', 3),
    (5, 'Nhân Viên Kinh Doanh', 4, 'LEVEL_4', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (
    id, user_id, employee_code, full_name, gender, email, department_id, position_id, manager_id, status, join_date, created_at
) VALUES
    (1, 1, 'EMP-ADMIN', 'Nguyen Van Admin', 'MALE', 'admin@company.com', 1, 1, NULL, 'ACTIVE', CURRENT_DATE - 730, now()),
    (2, 2, 'EMP-HR', 'Tran Thi HR', 'FEMALE', 'hr@company.com', 2, 2, 1, 'ACTIVE', CURRENT_DATE - 540, now()),
    (3, 3, 'EMP-MANAGER', 'Le Van Manager', 'MALE', 'manager@company.com', 3, 3, 1, 'ACTIVE', CURRENT_DATE - 400, now()),
    (4, 4, 'EMP-EMPLOYEE', 'Pham Thi Employee', 'FEMALE', 'employee@company.com', 3, 4, 3, 'ACTIVE', CURRENT_DATE - 180, now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('departments_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM departments), 1), TRUE);
SELECT setval('positions_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM positions), 1), TRUE);
SELECT setval('employees_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM employees), 1), TRUE);
