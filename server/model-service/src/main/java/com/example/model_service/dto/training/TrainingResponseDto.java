package com.example.model_service.dto.training;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrainingResponseDto {
    private String status;
    private String message;
    private Long datasetId;
    private String datasetName;
    private String exportDir;
    private String logFilePath;
    private String bestModelPath;
    private String dataYamlPath;
    private String modelOutputDir;
    private String runName;
    private Integer epochs;
    private Integer batchSize;
    private Double trainRatio;
    private Double valRatio;
    private Double testRatio;
}
