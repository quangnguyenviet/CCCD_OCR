package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoundingBoxRequestDto {

    @JsonAlias({"labelName", "label_name"})
    private String labelName;

    @JsonAlias({"xMin", "x_min"})
    private Double xMin;

    @JsonAlias({"yMin", "y_min"})
    private Double yMin;

    @JsonAlias({"xMax", "x_max"})
    private Double xMax;

    @JsonAlias({"yMax", "y_max"})
    private Double yMax;
}
