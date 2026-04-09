package com.example.model_service.controller;

import com.example.model_service.dto.labeling.CreateDatasetRequestDto;
import com.example.model_service.dto.labeling.SaveAnnotationsRequestDto;
import com.example.model_service.entity.LabeledImage;
import com.example.model_service.service.DatasetExportService;
import com.example.model_service.service.LabelingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/labeling/cccd")
public class LabelingController {

    private final LabelingService labelingService;
    private final DatasetExportService datasetExportService;

    public LabelingController(LabelingService labelingService, DatasetExportService datasetExportService) {
        this.labelingService = labelingService;
        this.datasetExportService = datasetExportService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("image_file") MultipartFile imageFile) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Upload ảnh thành công",
                "data", labelingService.uploadImage(imageFile)
        ));
    }

    @PostMapping("/{imageId}/annotations")
    public ResponseEntity<?> saveAnnotations(
            @PathVariable Long imageId,
            @RequestBody SaveAnnotationsRequestDto request
    ) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Lưu nhãn thành công",
                "data", labelingService.saveAnnotations(imageId, request)
        ));
    }

    @GetMapping("/images/annotated")
    public ResponseEntity<?> getAnnotatedImages() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Lấy danh sách mẫu đã gán nhãn thành công",
                "data", labelingService.getAnnotatedImages()
        ));
    }

    @GetMapping("/images/{imageId}/raw")
    public ResponseEntity<byte[]> getRawImage(@PathVariable Long imageId) {
        LabeledImage image = labelingService.getLabeledImageById(imageId);

        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (image.getMimeType() != null && !image.getMimeType().isBlank()) {
            try {
                contentType = MediaType.parseMediaType(image.getMimeType());
            } catch (Exception ignored) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(image.getImageData());
    }

    @PostMapping("/datasets")
    public ResponseEntity<?> createDataset(@RequestBody CreateDatasetRequestDto request) throws IOException {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Tạo dataset thành công",
                "data", datasetExportService.createDataset(request)
        ));
    }

    @GetMapping("/datasets")
    public ResponseEntity<?> getDatasets() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Lấy danh sách dataset thành công",
                "data", datasetExportService.getDatasets()
        ));
    }

    @GetMapping("/datasets/{datasetId}/export")
    public ResponseEntity<?> exportDataset(@PathVariable Long datasetId) {
        byte[] zipData = datasetExportService.exportDatasetById(datasetId);
        String fileName = datasetExportService.getDatasetFileNameById(datasetId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipData);
    }
}
