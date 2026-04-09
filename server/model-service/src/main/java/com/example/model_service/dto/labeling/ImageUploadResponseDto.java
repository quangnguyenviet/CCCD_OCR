package com.example.model_service.dto.labeling;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageUploadResponseDto {
    private Long imageId;
    private String originalFilename;
    private Integer imageWidth;
    private Integer imageHeight;
    private String imagePath;
}
