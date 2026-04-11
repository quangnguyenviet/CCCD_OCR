package com.example.training_service.dto.training;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TrainingResultListResponseDto {
    private String runDir;
    private List<TrainingResultImageDto> images;
}
