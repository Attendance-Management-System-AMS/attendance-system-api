-- Add status column to departments table
ALTER TABLE departments ADD COLUMN status VARCHAR(40) DEFAULT 'ACTIVE';

-- Update existing departments to have ACTIVE status if they are NULL
UPDATE departments SET status = 'ACTIVE' WHERE status IS NULL;
