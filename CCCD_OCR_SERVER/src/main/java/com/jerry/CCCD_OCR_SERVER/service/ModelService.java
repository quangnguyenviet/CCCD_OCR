package com.jerry.CCCD_OCR_SERVER.service;

import com.jerry.CCCD_OCR_SERVER.dto.ActiveModelsResponseDto;
import com.jerry.CCCD_OCR_SERVER.dto.ModelItemDto;
import com.jerry.CCCD_OCR_SERVER.entity.Model;
import com.jerry.CCCD_OCR_SERVER.enums.ModelType;
import com.jerry.CCCD_OCR_SERVER.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý logic liên quan đến mô hình AI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelService {
    
    private final ModelRepository modelRepository;
    
    /**
     * Lấy danh sách tất cả mô hình hoạt động được phân loại theo loại
     * @return ActiveModelsResponseDto chứa danh sách mô hình theo từng loại
     */
    public ActiveModelsResponseDto getActiveModels() {
        log.info("Fetching all active models");
        
        List<Model> cardDetectionModels = modelRepository
            .findByTypeAndIsActive(ModelType.CARD_DETECTION, true);
        List<Model> roiDetectionModels = modelRepository
            .findByTypeAndIsActive(ModelType.ROI_DETECTION, true);
        List<Model> ocrModels = modelRepository
            .findByTypeAndIsActive(ModelType.OCR, true);
        
        log.debug("Found {} card detection, {} ROI detection, {} OCR models",
            cardDetectionModels.size(), roiDetectionModels.size(), ocrModels.size());
        
        return new ActiveModelsResponseDto(
            convertToDto(cardDetectionModels),
            convertToDto(roiDetectionModels),
            convertToDto(ocrModels)
        );
    }
    
    /**
     * Chuyển đổi danh sách Model entity thành danh sách ModelItemDto
     * @param models danh sách Model
     * @return danh sách ModelItemDto
     */
    private List<ModelItemDto> convertToDto(List<Model> models) {
        return models.stream()
            .map(model -> new ModelItemDto(
                String.valueOf(model.getId()),
                model.getName()
            ))
            .collect(Collectors.toList());
    }
}
