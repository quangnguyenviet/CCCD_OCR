DO $$
BEGIN
    -- Create the enum type only if it doesn't already exist
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'model_type') THEN
        CREATE TYPE model_type AS ENUM ('CARD_DETECTION', 'ROI_DETECTION', 'OCR');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS ai_models (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type model_type NOT NULL,
    version VARCHAR(50),
    accuracy REAL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
