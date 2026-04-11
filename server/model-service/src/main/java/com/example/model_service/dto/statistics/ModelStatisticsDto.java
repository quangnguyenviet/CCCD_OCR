package com.example.model_service.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelStatisticsDto {
    private long totalModels;
    private Map<String, Long> modelsByType;
    private Map<String, Long> modelsByStatus;
}
