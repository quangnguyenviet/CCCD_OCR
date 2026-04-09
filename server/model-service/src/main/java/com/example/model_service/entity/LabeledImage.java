package com.example.model_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "labeled_images")
@Data
public class LabeledImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "image_width", nullable = false)
    private Integer imageWidth;

    @Column(name = "image_height", nullable = false)
    private Integer imageHeight;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
