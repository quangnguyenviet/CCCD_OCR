package com.example.model_service.dto.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AnnotatedImageItemDto {

    @JsonProperty("image_id")
    private Long imageId;

    @JsonProperty("original_filename")
    private String originalFilename;

    @JsonProperty("image_width")
    private Integer imageWidth;

    @JsonProperty("image_height")
    private Integer imageHeight;

    @JsonProperty("annotation_count")
    private Integer annotationCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
