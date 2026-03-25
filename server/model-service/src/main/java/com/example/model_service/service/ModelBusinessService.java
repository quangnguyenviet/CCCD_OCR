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

    private final ModelRepository modelRepository;

    public ActiveModelsResponseDto getActiveModels() {
        log.info("Fetching all active models");

        List<Model> cardDetectionModels = modelRepository
            .findByTypeAndIsActive(ModelType.CARD_DETECTION, true);
        List<Model> roiDetectionModels = modelRepository
            .findByTypeAndIsActive(ModelType.ROI_DETECTION, true);
        List<Model> ocrModels = modelRepository
            .findByTypeAndIsActive(ModelType.OCR, true);

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
                model.getUrl()
            ))
            .toList();
    }
}
