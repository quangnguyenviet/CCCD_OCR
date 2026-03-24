package com.jerry.CCCD_OCR_SERVER.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO đại diện cho danh sách các mô hình hoạt động được phân loại theo loại
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveModelsResponseDto {
    
    @JsonProperty("card_detection_models")
    private List<ModelItemDto> cardDetectionModels;
    
    @JsonProperty("roi_detection_models")
    private List<ModelItemDto> roiDetectionModels;
    
    @JsonProperty("ocr_models")
    private List<ModelItemDto> ocrModels;
}
