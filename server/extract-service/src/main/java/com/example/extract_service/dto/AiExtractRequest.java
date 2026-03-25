package com.example.extract_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiExtractRequest {

    @JsonProperty("card_model_url")
    private String cardModelUrl;

    @JsonProperty("roi_model_url")
    private String roiModelUrl;

    @JsonProperty("ocr_model_url")
    private String ocrModelUrl;

    @JsonProperty("image_url")
    private String imageUrl;
}
