-- ============================================================
-- attendance-service: V2__add_schedule_templates.sql
-- Thêm bảng mẫu lịch làm việc và các hạng mục mẫu
-- ============================================================

CREATE TABLE schedule_templates (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255)
);

CREATE TABLE schedule_template_items (
    id              BIGSERIAL       PRIMARY KEY,
    template_id     BIGINT          NOT NULL REFERENCES schedule_templates(id) ON DELETE CASCADE,
    day_of_week     INTEGER         NOT NULL,
    shift_id        BIGINT          NOT NULL REFERENCES shifts(id)
);
