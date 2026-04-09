package com.example.model_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "dataset_exports")
@Data
public class DatasetExport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_name", nullable = false)
    private String datasetName;

    @Column(name = "zip_file_name", nullable = false)
    private String zipFileName;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "zip_data", nullable = false)
    private byte[] zipData;

    @Column(name = "total_images", nullable = false)
    private Integer totalImages;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
