-- Add training metadata columns to ai_models table
ALTER TABLE ai_models
ADD COLUMN IF NOT EXISTS training_start_time TIMESTAMP,
ADD COLUMN IF NOT EXISTS training_end_time TIMESTAMP,
ADD COLUMN IF NOT EXISTS training_duration_seconds INTEGER,
ADD COLUMN IF NOT EXISTS final_loss DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS final_metrics TEXT;
