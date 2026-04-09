package com.example.model_service.dto.labeling;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoundingBoxResponseDto {
    private Long id;
    private Long imageId;
    private String labelName;
    private Double xMin;
    private Double yMin;
    private Double xMax;
    private Double yMax;
}
