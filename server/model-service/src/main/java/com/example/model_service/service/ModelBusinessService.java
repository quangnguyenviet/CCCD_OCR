package com.example.model_service.service;

import com.example.model_service.dto.ActiveModelsResponseDto;
import com.example.model_service.dto.ModelItemDto;
import com.example.model_service.entity.AiModel;
import com.example.model_service.enums.ModelType;
import com.example.model_service.repository.AiModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelBusinessService {

    private final AiModelRepository aiModelRepository;

    public ActiveModelsResponseDto getActiveModels() {
        log.info("Fetching all active models");

        List<AiModel> cardDetectionModels = aiModelRepository
            .findByTypeAndStatus(ModelType.CARD_DETECTION.toString(), AiModel.Status.SUCCESS);
        List<AiModel> roiDetectionModels = aiModelRepository
            .findByTypeAndStatus(ModelType.ROI_DETECTION.toString(), AiModel.Status.SUCCESS);
        List<AiModel> ocrModels = aiModelRepository
            .findByTypeAndStatus(ModelType.OCR.toString(), AiModel.Status.SUCCESS);

        return new ActiveModelsResponseDto(
            convertToDto(cardDetectionModels),
            convertToDto(roiDetectionModels),
            convertToDto(ocrModels)
        );
    }

    private List<ModelItemDto> convertToDto(List<AiModel> models) {
        return models.stream()
            .map(model -> new ModelItemDto(
                String.valueOf(model.getId()),
                model.getName(),
                model.getModelFilePath()
            ))
            .toList();
    }
}
