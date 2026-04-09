package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DatasetSummaryDto {

    @JsonProperty("dataset_id")
    private Long datasetId;

    @JsonProperty("dataset_name")
    private String datasetName;

    @JsonProperty("zip_file_name")
    private String zipFileName;

    @JsonProperty("total_images")
    private Integer totalImages;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
