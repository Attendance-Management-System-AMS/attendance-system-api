-- ============================================================
-- ATTENDANCE SERVICE: V2__seed_sample_data.sql
-- Seed sample schedules, holidays, attendances and logs.
-- employee_id values must match hr-service employees.id.
-- ============================================================

INSERT INTO holidays (id, holiday_name, from_date, to_date, is_paid) VALUES
    (1, 'Nghi le local demo', CURRENT_DATE + 7, CURRENT_DATE + 7, TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employee_schedules (id, employee_id, shift_id, day_of_week, is_active, effective_from) VALUES
    (1, 2, 1, 1, TRUE, CURRENT_DATE - 60),
    (2, 2, 1, 2, TRUE, CURRENT_DATE - 60),
    (3, 2, 1, 3, TRUE, CURRENT_DATE - 60),
    (4, 2, 1, 4, TRUE, CURRENT_DATE - 60),
    (5, 2, 1, 5, TRUE, CURRENT_DATE - 60),
    (6, 3, 1, 1, TRUE, CURRENT_DATE - 60),
    (7, 3, 1, 2, TRUE, CURRENT_DATE - 60),
    (8, 3, 1, 3, TRUE, CURRENT_DATE - 60),
    (9, 3, 1, 4, TRUE, CURRENT_DATE - 60),
    (10, 3, 1, 5, TRUE, CURRENT_DATE - 60),
    (11, 4, 1, 1, TRUE, CURRENT_DATE - 60),
    (12, 4, 1, 2, TRUE, CURRENT_DATE - 60),
    (13, 4, 1, 3, TRUE, CURRENT_DATE - 60),
    (14, 4, 1, 4, TRUE, CURRENT_DATE - 60),
    (15, 4, 1, 5, TRUE, CURRENT_DATE - 60)
ON CONFLICT (id) DO NOTHING;

INSERT INTO attendances (
    id, employee_id, check_in_time, check_out_time, work_date, status,
    late_minutes, early_leave_minutes, worked_minutes, expected_minutes, created_at
) VALUES
    (1, 2, (CURRENT_DATE - 2) + TIME '08:00', (CURRENT_DATE - 2) + TIME '17:02',
        CURRENT_DATE - 2, 'PRESENT', 0, 0, 482, 480, now()),
    (2, 3, (CURRENT_DATE - 2) + TIME '08:17', (CURRENT_DATE - 2) + TIME '17:00',
        CURRENT_DATE - 2, 'LATE', 17, 0, 463, 480, now()),
    (3, 4, (CURRENT_DATE - 2) + TIME '08:03', (CURRENT_DATE - 2) + TIME '16:40',
        CURRENT_DATE - 2, 'EARLY_LEAVE', 0, 20, 457, 480, now()),
    (4, 2, (CURRENT_DATE - 1) + TIME '08:02', (CURRENT_DATE - 1) + TIME '17:04',
        CURRENT_DATE - 1, 'PRESENT', 0, 0, 482, 480, now()),
    (5, 3, NULL, NULL, CURRENT_DATE - 1, 'ABSENT', 0, 0, 0, 480, now()),
    (6, 4, (CURRENT_DATE - 1) + TIME '08:09', (CURRENT_DATE - 1) + TIME '17:01',
        CURRENT_DATE - 1, 'PRESENT', 0, 0, 472, 480, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO attendance_logs (id, employee_id, log_time, device_id, log_type, created_at) VALUES
    (1, 2, (CURRENT_DATE - 2) + TIME '08:00', 'WEB', 'IN', now()),
    (2, 2, (CURRENT_DATE - 2) + TIME '17:02', 'WEB', 'OUT', now()),
    (3, 3, (CURRENT_DATE - 2) + TIME '08:17', 'WEB', 'IN', now()),
    (4, 3, (CURRENT_DATE - 2) + TIME '17:00', 'WEB', 'OUT', now()),
    (5, 4, (CURRENT_DATE - 2) + TIME '08:03', 'WEB', 'IN', now()),
    (6, 4, (CURRENT_DATE - 2) + TIME '16:40', 'WEB', 'OUT', now()),
    (7, 2, (CURRENT_DATE - 1) + TIME '08:02', 'WEB', 'IN', now()),
    (8, 2, (CURRENT_DATE - 1) + TIME '17:04', 'WEB', 'OUT', now()),
    (9, 4, (CURRENT_DATE - 1) + TIME '08:09', 'WEB', 'IN', now()),
    (10, 4, (CURRENT_DATE - 1) + TIME '17:01', 'WEB', 'OUT', now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('holidays_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM holidays), 1), TRUE);
SELECT setval('employee_schedules_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM employee_schedules), 1), TRUE);
SELECT setval('attendances_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM attendances), 1), TRUE);
SELECT setval('attendance_logs_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM attendance_logs), 1), TRUE);
