package com.example.model_service.dto.dataset;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateDatasetRequestDto {
    private String datasetName;
    private List<Long> imageIds;
}
