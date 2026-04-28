-- ============================================================
-- AUTH SERVICE: V2__seed_sample_data.sql
-- Seed sample users and role mappings for local development.
-- ============================================================

INSERT INTO users (id, username, password, email, is_enabled, created_at) VALUES
    (1, 'admin', '$2a$10$eQT9ACiVWYRtvPtbaBiJruDTnkqjT5k5S/gFt4vDjP29u.8V5.Lja', 'admin@company.com', TRUE, now()),
    (2, 'hr', '$2a$10$6QnLgVhVyabWzj/cuo2UeuMi2Q6lH2wBC1tYeWtpsH6ZTN8sb7x6C', 'hr@company.com', TRUE, now()),
    (3, 'manager', '$2a$10$6rxVtOY4zLMbQxiIJ.yMeOA6ypT5nwe0RE6/tCOaXTO9Bm6CT9aRm', 'manager@company.com', TRUE, now()),
    (4, 'employee', '$2a$10$ifwvNS4ZgYl/vcWzd16qou1Njwt9irKGIxYk04ttrHgQ9sHo9rPYS', 'employee@company.com', TRUE, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 4),
    (2, 3),
    (3, 2),
    (4, 1)
ON CONFLICT (user_id, role_id) DO NOTHING;

SELECT setval('users_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM users), 1), TRUE);
