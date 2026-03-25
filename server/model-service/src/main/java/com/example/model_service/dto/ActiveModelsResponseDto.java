package com.example.model_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
