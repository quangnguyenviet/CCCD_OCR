package com.example.model_service.entity;

import com.example.model_service.enums.ModelType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "ai_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModelType type;

    @Column(name = "epochs")
    private Integer epochs;

    @Column(name = "batch_size")
    private Integer batchSize;

    @Column(name = "model_file_path", length = 255)
    private String modelFilePath;

    @Column(length = 1024)
    private String url;

    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "latest_log", columnDefinition = "TEXT")
    private String latestLog;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime createdAt;
}
