CREATE TABLE labeled_images (
    id BIGSERIAL PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),
    image_width INTEGER NOT NULL,
    image_height INTEGER NOT NULL,
    image_data BYTEA NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE labeled_bounding_boxes (
    id BIGSERIAL PRIMARY KEY,
    labeled_image_id BIGINT NOT NULL,
    label_name VARCHAR(100) NOT NULL,
    x_min DOUBLE PRECISION NOT NULL,
    y_min DOUBLE PRECISION NOT NULL,
    x_max DOUBLE PRECISION NOT NULL,
    y_max DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_labeled_image
        FOREIGN KEY (labeled_image_id)
        REFERENCES labeled_images(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_labeled_bounding_boxes_image_id
    ON labeled_bounding_boxes(labeled_image_id);