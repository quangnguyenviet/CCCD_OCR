package com.example.model_service.dto.dataset;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ImageOptionDto {
    private Long id;
    private String originalFilename;
    private Integer imageWidth;
    private Integer imageHeight;
    private String previewUrl;
    private Long boundingBoxCount;
    private LocalDateTime createdAt;
}
