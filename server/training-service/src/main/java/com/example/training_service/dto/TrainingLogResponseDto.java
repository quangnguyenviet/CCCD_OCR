package com.example.training_service.dto.training;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrainingLogResponseDto {
    private String logFilePath;
    private boolean exists;
    private String content;
}
