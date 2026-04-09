package com.example.model_service.dto.model;

import com.example.model_service.enums.ModelType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterTrainedModelRequestDto {
    private String name;
    private ModelType type;
    private Long datasetId;
    private String modelFilePath;
    private String url;
    private Integer epochs;
    private Integer batchSize;
    private String trainingLogPath;
}
