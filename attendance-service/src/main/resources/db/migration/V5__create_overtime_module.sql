ALTER TABLE attendances
    ADD COLUMN IF NOT EXISTS actual_overtime_minutes INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS approved_overtime_minutes INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS payable_overtime_minutes INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS overtime_status VARCHAR(30) NOT NULL DEFAULT 'NONE';

CREATE TABLE IF NOT EXISTS overtime_requests (
    id                  BIGSERIAL       PRIMARY KEY,
    employee_id          BIGINT          NOT NULL,
    work_date            DATE            NOT NULL,
    start_time           TIME            NOT NULL,
    end_time             TIME            NOT NULL,
    requested_minutes    INTEGER         NOT NULL,
    reason               VARCHAR(500),
    status               VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    approved_by          BIGINT,
    approved_at          TIMESTAMP,
    approval_note        VARCHAR(500),
    created_at           TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_overtime_requests_employee_date
    ON overtime_requests (employee_id, work_date);

CREATE INDEX IF NOT EXISTS idx_overtime_requests_status_date
    ON overtime_requests (status, work_date);

CREATE INDEX IF NOT EXISTS idx_overtime_requests_approver
    ON overtime_requests (approved_by);
