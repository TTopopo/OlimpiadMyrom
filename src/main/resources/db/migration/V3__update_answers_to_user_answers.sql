-- Drop the old answers table
DROP TABLE IF EXISTS answers;

-- Add result_id to user_answers table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'user_answers' 
        AND column_name = 'result_id'
    ) THEN
        ALTER TABLE user_answers ADD COLUMN result_id BIGINT REFERENCES results(id) ON DELETE SET NULL;
    END IF;
END $$; 