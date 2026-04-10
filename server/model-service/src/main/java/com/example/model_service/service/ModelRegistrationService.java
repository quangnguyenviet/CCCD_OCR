package com.example.model_service.service;

import com.example.model_service.dto.model.RegisterTrainedModelRequestDto;
import com.example.model_service.dto.model.RegisterTrainedModelResponseDto;
import com.example.model_service.entity.Model;
import com.example.model_service.repository.DatasetRepository;
import com.example.model_service.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ModelRegistrationService {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final ModelRepository modelRepository;
    private final DatasetRepository datasetRepository;
    private final TrainingMetricsService trainingMetricsService;

    @Transactional
    public RegisterTrainedModelResponseDto registerTrainedModel(RegisterTrainedModelRequestDto request) {
        validateRequest(request);

        datasetRepository.findById(request.getDatasetId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dataset với id=" + request.getDatasetId()));

        Model model = new Model();
        model.setName(request.getName().trim());
        model.setType(request.getType());
        model.setEpochs(request.getEpochs());
        model.setBatchSize(request.getBatchSize());
        model.setModelFilePath(request.getModelFilePath());
        model.setUrl(request.getUrl() == null || request.getUrl().isBlank() ? request.getModelFilePath() : request.getUrl());
        model.setDatasetId(request.getDatasetId());
        model.setStatus(STATUS_COMPLETED);
        model.setProgressPercent(100);
        model.setLatestLog(request.getTrainingLogPath());
        
        // Set training metadata
        LocalDateTime now = LocalDateTime.now();
        model.setTrainingStartTime(now);
        model.setTrainingEndTime(now);

        Model saved = modelRepository.save(model);
        
        // Parse and save training metrics from log file
        if (request.getTrainingLogPath() != null && !request.getTrainingLogPath().isBlank()) {
            trainingMetricsService.saveTrainingMetrics(saved.getId(), now, request.getTrainingLogPath());
        }
        
        return new RegisterTrainedModelResponseDto(
            saved.getId(),
            saved.getName(),
            saved.getType().name(),
            saved.getStatus(),
            saved.getModelFilePath()
        );
    }

    private void validateRequest(RegisterTrainedModelRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request không hợp lệ.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Tên model không được để trống.");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại model.");
        }
        if (request.getDatasetId() == null) {
            throw new IllegalArgumentException("Thiếu datasetId.");
        }
        if (request.getModelFilePath() == null || request.getModelFilePath().isBlank()) {
            throw new IllegalArgumentException("Thiếu đường dẫn model huấn luyện.");
        }
    }
}
