package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveBoundingBoxesRequestDto {

    @JsonAlias({"boxes", "bounding_boxes"})
    private List<BoundingBoxRequestDto> boxes;
}
