package com.example.model_service.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterTrainedModelResponseDto {
    private Long id;
    private String name;
    private String type;
    private String status;
    private String modelFilePath;
}
