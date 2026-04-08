CREATE TABLE ai_models (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255),
                           type VARCHAR(50) DEFAULT 'ROI_DETECTION',

                           epochs INTEGER,
                           batch_size INTEGER,
                        model_file_path VARCHAR(255),

                           dataset_id BIGINT,

                           status VARCHAR(20) DEFAULT 'PENDING',

                           progress_percent INTEGER DEFAULT 0,

                           latest_log TEXT,

                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);