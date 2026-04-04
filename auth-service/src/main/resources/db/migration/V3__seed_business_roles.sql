-- ============================================================
-- auth-service: V3__seed_business_roles.sql
-- Đồng bộ bộ role theo nghiệp vụ chấm công
-- ============================================================

-- Chuyển role cũ ROLE_USER thành ROLE_EMPLOYEE nếu tồn tại
UPDATE roles
SET role_name = 'ROLE_EMPLOYEE',
    description = 'Nhân viên thông thường'
WHERE role_name = 'ROLE_USER';

INSERT INTO roles (role_name, description)
VALUES ('ROLE_EMPLOYEE', 'Nhân viên thông thường')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name, description)
VALUES ('ROLE_MANAGER', 'Quản lý phòng ban')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name, description)
VALUES ('ROLE_HR', 'Nhân sự và vận hành chấm công')
ON CONFLICT (role_name) DO NOTHING;

UPDATE roles
SET description = 'Quản trị và kiểm toán hệ thống'
WHERE role_name = 'ROLE_ADMIN';
