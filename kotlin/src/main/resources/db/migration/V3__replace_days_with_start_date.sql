ALTER TABLE time_deposits
    ADD COLUMN start_date DATE NOT NULL DEFAULT CURRENT_DATE;

UPDATE time_deposits
SET start_date = CURRENT_DATE - days;

ALTER TABLE time_deposits
    DROP COLUMN days;
