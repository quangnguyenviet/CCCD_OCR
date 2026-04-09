package com.example.model_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dataset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatasetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_name", nullable = false, length = 255)
    private String datasetName;

    @Column(name = "zip_file_name", length = 255)
    private String zipFileName;

    @Column(name = "zip_file_path", length = 255)
    private String zipFilePath;

    @Column(name = "total_images", nullable = false)
    private Integer totalImages;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
