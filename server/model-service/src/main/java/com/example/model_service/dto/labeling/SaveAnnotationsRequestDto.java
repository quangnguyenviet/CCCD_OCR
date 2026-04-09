package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SaveAnnotationsRequestDto {

    @JsonProperty("annotations")
    private List<BoundingBoxRequestDto> annotations;
}
