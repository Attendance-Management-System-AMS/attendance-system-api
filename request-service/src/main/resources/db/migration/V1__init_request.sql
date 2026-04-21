-- ============================================================
-- REQUEST SERVICE: V1__init_request.sql
-- Owns leave types and leave requests.
-- employee_id and approved_by are logical references to hr-service.
-- ============================================================

CREATE TABLE leave_types (
    id                    BIGSERIAL       PRIMARY KEY,
    code                  VARCHAR(10)     UNIQUE NOT NULL,
    name                  VARCHAR(100)    NOT NULL,
    is_paid               BOOLEAN         NOT NULL DEFAULT TRUE,
    deduct_annual_leave   BOOLEAN         NOT NULL DEFAULT TRUE,
    insurance_covers      BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active             BOOLEAN         NOT NULL DEFAULT TRUE,
    description           VARCHAR(500)
);

CREATE TABLE leave_requests (
    id              BIGSERIAL        PRIMARY KEY,
    employee_id     BIGINT           NOT NULL,
    leave_type_id   BIGINT           NOT NULL REFERENCES leave_types(id),
    from_date       DATE             NOT NULL,
    to_date         DATE             NOT NULL,
    total_days      DOUBLE PRECISION NOT NULL,
    reason          VARCHAR(500),
    status          VARCHAR(20)      NOT NULL DEFAULT 'PENDING',
    approved_by     BIGINT,
    created_at      TIMESTAMP        DEFAULT now()
);

CREATE INDEX idx_leave_requests_employee_id ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_date_range ON leave_requests(from_date, to_date);

INSERT INTO leave_types (code, name, is_paid, deduct_annual_leave, insurance_covers, is_active, description) VALUES
('AL', 'Nghỉ phép năm', true, true, false, true, 'Nghỉ phép hưởng lương định kỳ hàng năm'),
('CL', 'Nghỉ chế độ (Ốm)', false, false, true, true, 'Nghỉ ốm đau có bảo hiểm chi trả'),
('UL', 'Nghỉ không lương', false, false, false, true, 'Nghỉ việc riêng không hưởng lương'),
('ML', 'Nghỉ thai sản', true, false, true, true, 'Nghỉ thai sản theo quy định nhà nước');
