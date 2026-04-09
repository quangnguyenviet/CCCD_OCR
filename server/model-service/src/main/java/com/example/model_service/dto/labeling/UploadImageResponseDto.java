package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadImageResponseDto {

    @JsonProperty("image_id")
    private Long imageId;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("image_width")
    private Integer imageWidth;

    @JsonProperty("image_height")
    private Integer imageHeight;
}
