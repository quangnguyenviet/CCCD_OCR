package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CreateDatasetRequestDto {

    @JsonProperty("dataset_name")
    private String datasetName;

    @JsonProperty("image_ids")
    private List<Long> imageIds;
}
