-- Thêm 5 nhân viên mẫu
INSERT INTO employees (id, employee_code, full_name, email, department_id, position_id, status, join_date, created_at) VALUES
(101, 'EMP-0101', 'Trần Trí Tuệ', 'tran.tue@company.com', 3, 4, 'ACTIVE', '2026-01-01', now()),
(102, 'EMP-0102', 'Lý Lệ Lệ', 'ly.le@company.com', 3, 4, 'ACTIVE', '2026-01-01', now()),
(103, 'EMP-0103', 'Hoàng Kim Ngọc', 'hoang.ngoc@company.com', 3, 4, 'ACTIVE', '2026-01-01', now()),
(104, 'EMP-0104', 'Vũ Văn Việt', 'vu.viet@company.com', 3, 4, 'ACTIVE', '2026-01-01', now()),
(105, 'EMP-0105', 'Đặng Đình Đồng', 'dang.dong@company.com', 3, 4, 'ACTIVE', '2026-01-01', now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('employees_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM employees), 1), TRUE);
