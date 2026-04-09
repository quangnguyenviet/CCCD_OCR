package com.example.model_service.dto.training;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainingRequestDto {
    private Long datasetId;
    private Integer epochs;
    private Integer batchSize;
    private Double trainRatio;
    private Double valRatio;
    private Double testRatio;
    private Integer imageSize;
    private String baseWeights;
    private String projectName;
    private String runName;
}
