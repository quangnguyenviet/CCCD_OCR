package com.example.model_service.dto.dataset;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateDatasetResponseDto {
    private Long id;
    private String datasetName;
    private Integer totalImages;
    private LocalDateTime createdAt;
}
