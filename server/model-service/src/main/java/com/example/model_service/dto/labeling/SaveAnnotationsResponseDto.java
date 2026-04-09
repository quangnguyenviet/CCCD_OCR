package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveAnnotationsResponseDto {

    @JsonProperty("image_id")
    private Long imageId;

    @JsonProperty("total_annotations")
    private Integer totalAnnotations;
}
