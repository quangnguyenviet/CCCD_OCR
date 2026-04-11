package com.example.training_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "model-service", url = "http://localhost:8082")
public interface DataServiceClient {

    @GetMapping("/api/internal/datasets/{datasetId}/export")
    DatasetExportDto getDatasetExportDetails(@PathVariable("datasetId") Long datasetId);

    record DatasetExportDto(
            Long datasetId,
            String datasetName,
            List<ImageExportDto> images
    ) {}

    record ImageExportDto(
            Long imageId,
            String imagePath,
            String originalFilename,
            Integer imageWidth,
            Integer imageHeight,
            List<BoundingBoxExportDto> boxes
    ) {}

    record BoundingBoxExportDto(
            Long boxId,
            String labelName,
            Double xMin,
            Double yMin,
            Double xMax,
            Double yMax
    ) {}
}
