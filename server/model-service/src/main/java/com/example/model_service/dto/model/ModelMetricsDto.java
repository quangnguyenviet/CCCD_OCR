package com.example.model_service.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelMetricsDto {
    private Long id;
    private String name;
    private String type;
    private Integer epochs;
    private Integer batchSize;
    private String status;
    private LocalDateTime trainingStartTime;
    private LocalDateTime trainingEndTime;
    private Integer trainingDurationSeconds;
    private Double finalLoss;
    private String finalMetrics;
    private LocalDateTime createdAt;
}
