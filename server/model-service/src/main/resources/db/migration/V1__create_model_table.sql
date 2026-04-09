-- =========================
-- DATASET
-- =========================
CREATE TABLE dataset (
    id BIGSERIAL PRIMARY KEY,
    dataset_name VARCHAR(255) NOT NULL,
    zip_file_name VARCHAR(255),
    zip_file_path VARCHAR(255),
    total_images INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- IMAGES
-- =========================
CREATE TABLE images (
    id BIGSERIAL PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),
    image_width INTEGER NOT NULL,
    image_height INTEGER NOT NULL,
    image_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- BOUNDING BOXES
-- =========================
CREATE TABLE bounding_boxes (
    id BIGSERIAL PRIMARY KEY,
    image_id BIGINT NOT NULL,
    label_name VARCHAR(100) NOT NULL,
    x_min DOUBLE PRECISION NOT NULL,
    y_min DOUBLE PRECISION NOT NULL,
    x_max DOUBLE PRECISION NOT NULL,
    y_max DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_image
        FOREIGN KEY (image_id)
        REFERENCES images(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_bbox_valid
        CHECK (x_min < x_max AND y_min < y_max)
);

CREATE INDEX idx_bounding_boxes_image_id
ON bounding_boxes(image_id);

-- =========================
-- DATASET ITEMS (mapping image -> dataset)
-- =========================
CREATE TABLE dataset_items (
    id BIGSERIAL PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    image_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dataset_items_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES dataset(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_dataset_items_image
        FOREIGN KEY (image_id)
        REFERENCES images(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_dataset_items_dataset_id
ON dataset_items(dataset_id);

CREATE INDEX idx_dataset_items_image_id
ON dataset_items(image_id);

-- =========================
-- AI MODELS
-- =========================
CREATE TABLE ai_models (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) DEFAULT 'ROI_DETECTION',
    epochs INTEGER,
    batch_size INTEGER,
    model_file_path VARCHAR(255),
    dataset_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    progress_percent INTEGER DEFAULT 0,
    latest_log TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_models_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES dataset(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_status_valid
        CHECK (status IN ('PENDING', 'TRAINING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_ai_models_dataset_id
ON ai_models(dataset_id);
