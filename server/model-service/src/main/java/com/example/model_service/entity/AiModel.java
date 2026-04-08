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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_models")
@Data // Dùng Lombok để tự gen Getter/Setter
public class AiModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type = "ROI_DETECTION";

    private Integer epochs;
    private Integer batchSize;

    private String modelFilePath;

    // Lưu ý: dataset_id có thể null vì luồng này dùng file ZIP trực tiếp
    @Column(name = "dataset_id")
    private Long datasetId;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    @Column(name = "latest_log")
    private String latestLog;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING, EXTRACTING, TRAINING, SUCCESS, FAILED, STOPPED
    }
}
