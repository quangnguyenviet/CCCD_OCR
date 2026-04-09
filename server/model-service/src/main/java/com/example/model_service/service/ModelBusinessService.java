package com.example.model_service.service;

import com.example.model_service.dto.ActiveModelsResponseDto;
import com.example.model_service.dto.ModelItemDto;
import com.example.model_service.entity.Model;
import com.example.model_service.enums.ModelType;
import com.example.model_service.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelBusinessService {

    private static final String MODEL_STATUS_COMPLETED = "COMPLETED";

    private final ModelRepository modelRepository;

    public ActiveModelsResponseDto getActiveModels() {
        log.info("Fetching all active models");

        List<Model> cardDetectionModels = modelRepository
            .findByTypeAndStatus(ModelType.CARD_DETECTION, MODEL_STATUS_COMPLETED);
        List<Model> roiDetectionModels = modelRepository
            .findByTypeAndStatus(ModelType.ROI_DETECTION, MODEL_STATUS_COMPLETED);
        List<Model> ocrModels = modelRepository
            .findByTypeAndStatus(ModelType.OCR, MODEL_STATUS_COMPLETED);

        return new ActiveModelsResponseDto(
            convertToDto(cardDetectionModels),
            convertToDto(roiDetectionModels),
            convertToDto(ocrModels)
        );
    }

    private List<ModelItemDto> convertToDto(List<Model> models) {
        return models.stream()
            .map(model -> new ModelItemDto(
                String.valueOf(model.getId()),
                model.getName(),
                model.getUrl() != null && !model.getUrl().isBlank() ? model.getUrl() : model.getModelFilePath()
            ))
            .toList();
    }
}
