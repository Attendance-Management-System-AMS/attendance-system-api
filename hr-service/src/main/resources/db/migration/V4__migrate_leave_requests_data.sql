-- ============================================================
-- hr-service: V4__migrate_leave_requests_data.sql
-- Migrate leave_type data from string to foreign key
-- ============================================================

-- Update leave_requests with leave_type_id based on old leave_type values
-- Map legacy leave_type strings to new leave_type IDs
UPDATE leave_requests
SET leave_type_id = (
    CASE
        WHEN leave_type = 'ANNUAL' THEN (SELECT id FROM leave_types WHERE code = 'AL')
        WHEN leave_type = 'AL' THEN (SELECT id FROM leave_types WHERE code = 'AL')
        WHEN leave_type = 'SICK' THEN (SELECT id FROM leave_types WHERE code = 'SL')
        WHEN leave_type = 'SL' THEN (SELECT id FROM leave_types WHERE code = 'SL')
        WHEN leave_type = 'UNPAID' THEN (SELECT id FROM leave_types WHERE code = 'UL')
        WHEN leave_type = 'UL' THEN (SELECT id FROM leave_types WHERE code = 'UL')
        WHEN leave_type = 'PH' THEN (SELECT id FROM leave_types WHERE code = 'PH')
        WHEN leave_type = 'ML' THEN (SELECT id FROM leave_types WHERE code = 'ML')
        WHEN leave_type = 'BT' THEN (SELECT id FROM leave_types WHERE code = 'BT')
        ELSE (SELECT id FROM leave_types WHERE code = 'UL')
    END
)
WHERE leave_type IS NOT NULL;

-- Add NOT NULL constraint to leave_type_id
ALTER TABLE leave_requests
ALTER COLUMN leave_type_id SET NOT NULL;

-- Drop the old leave_type column
ALTER TABLE leave_requests
DROP COLUMN leave_type;
