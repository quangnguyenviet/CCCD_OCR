package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnnotationItemDto {

    @JsonProperty("label_name")
    private String labelName;

    @JsonProperty("x_min")
    private Double xMin;

    @JsonProperty("y_min")
    private Double yMin;

    @JsonProperty("x_max")
    private Double xMax;

    @JsonProperty("y_max")
    private Double yMax;
}
