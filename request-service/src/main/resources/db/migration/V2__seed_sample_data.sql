-- ============================================================
-- REQUEST SERVICE: V2__seed_sample_data.sql
-- Seed sample leave requests.
-- employee_id and approved_by must match hr-service employees.id.
-- ============================================================

INSERT INTO leave_requests (
    id, employee_id, leave_type_id, from_date, to_date, total_days, reason, status, approved_by, created_at
) VALUES
    (1, 4, 1, CURRENT_DATE + 1, CURRENT_DATE + 2, 2, 'Du lich gia dinh', 'PENDING', NULL, now()),
    (2, 4, 2, CURRENT_DATE - 10, CURRENT_DATE - 9, 2, 'Nghi om co giay xac nhan', 'APPROVED', 2, now()),
    (3, 3, 3, CURRENT_DATE - 4, CURRENT_DATE - 4, 1, 'Cong tac ca nhan', 'REJECTED', 2, now()),
    (4, 2, 1, CURRENT_DATE + 7, CURRENT_DATE + 7, 1, 'Nghi phep ngan ngay', 'APPROVED', 1, now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('leave_requests_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM leave_requests), 1), TRUE);
