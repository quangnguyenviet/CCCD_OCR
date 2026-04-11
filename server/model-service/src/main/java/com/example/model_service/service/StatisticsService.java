package com.example.model_service.service;

import com.example.model_service.dto.statistics.ModelStatisticsDto;
import com.example.model_service.enums.ModelType;
import com.example.model_service.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ModelRepository modelRepository;

    public ModelStatisticsDto getModelStatistics() {
        long totalModels = modelRepository.count();

        List<Object[]> typesCount = modelRepository.countModelsByType();
        Map<String, Long> modelsByType = new HashMap<>();
        for (Object[] row : typesCount) {
            ModelType type = (ModelType) row[0];
            Long count = ((Number) row[1]).longValue();
            modelsByType.put(type != null ? type.name() : "UNKNOWN", count);
        }

        List<Object[]> statusCount = modelRepository.countModelsByStatus();
        Map<String, Long> modelsByStatus = new HashMap<>();
        for (Object[] row : statusCount) {
            String status = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            modelsByStatus.put(status != null ? status : "UNKNOWN", count);
        }

        return ModelStatisticsDto.builder()
                .totalModels(totalModels)
                .modelsByType(modelsByType)
                .modelsByStatus(modelsByStatus)
                .build();
    }
}
