CREATE TABLE dataset_exports (
    id BIGSERIAL PRIMARY KEY,
    dataset_name VARCHAR(255) NOT NULL,
    zip_file_name VARCHAR(255) NOT NULL,
    zip_data BYTEA NOT NULL,
    total_images INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dataset_export_items (
    id BIGSERIAL PRIMARY KEY,
    dataset_export_id BIGINT NOT NULL,
    labeled_image_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dataset_export_item_dataset
        FOREIGN KEY (dataset_export_id)
        REFERENCES dataset_exports(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dataset_export_item_image
        FOREIGN KEY (labeled_image_id)
        REFERENCES labeled_images(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_dataset_export_items_dataset_id
    ON dataset_export_items(dataset_export_id);

CREATE INDEX idx_dataset_export_items_image_id
    ON dataset_export_items(labeled_image_id);
