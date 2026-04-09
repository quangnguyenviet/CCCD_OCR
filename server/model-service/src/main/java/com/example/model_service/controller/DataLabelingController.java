package com.example.model_service.controller;

import com.example.model_service.dto.labeling.BoundingBoxResponseDto;
import com.example.model_service.dto.labeling.ImageUploadResponseDto;
import com.example.model_service.service.DataLabelingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labeling")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DataLabelingController {

    private final DataLabelingService dataLabelingService;

    @PostMapping("/images/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("image_file") MultipartFile imageFile) {
        try {
            ImageUploadResponseDto response = dataLabelingService.uploadImage(imageFile);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Upload image failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể tải ảnh lên."));
        }
    }

    @PostMapping("/images/{imageId}/boxes")
    public ResponseEntity<?> saveBoundingBoxes(
        @PathVariable Long imageId,
        @RequestBody JsonNode request
    ) {
        try {
            List<com.example.model_service.dto.labeling.BoundingBoxRequestDto> requestBoxes = parseBoxes(request);
            log.info("saveBoundingBoxes imageId={}, boxesCount={}", imageId, requestBoxes.size());
            if (!requestBoxes.isEmpty()) {
                var first = requestBoxes.get(0);
                log.info(
                    "firstBox label={}, xMin={}, yMin={}, xMax={}, yMax={}",
                    first.getLabelName(),
                    first.getXMin(),
                    first.getYMin(),
                    first.getXMax(),
                    first.getYMax()
                );
            }

            List<BoundingBoxResponseDto> response = dataLabelingService
                .saveBoundingBoxes(imageId, requestBoxes);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Save bounding boxes failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể lưu bounding box."));
        }
    }

    @GetMapping("/images/{imageId}/boxes")
    public ResponseEntity<?> getBoundingBoxes(@PathVariable Long imageId) {
        try {
            List<BoundingBoxResponseDto> response = dataLabelingService.getBoundingBoxes(imageId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get bounding boxes failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể lấy danh sách bounding box."));
        }
    }

    private List<com.example.model_service.dto.labeling.BoundingBoxRequestDto> parseBoxes(JsonNode request) {
        JsonNode boxesNode = request == null ? null : request.get("boxes");
        if (boxesNode == null || !boxesNode.isArray()) {
            boxesNode = request == null ? null : request.get("bounding_boxes");
        }

        if (boxesNode == null || !boxesNode.isArray()) {
            return List.of();
        }

        List<com.example.model_service.dto.labeling.BoundingBoxRequestDto> boxes = new java.util.ArrayList<>();

        for (JsonNode item : boxesNode) {
            com.example.model_service.dto.labeling.BoundingBoxRequestDto box = new com.example.model_service.dto.labeling.BoundingBoxRequestDto();
            box.setLabelName(readText(item, "labelName", "label_name"));
            box.setXMin(readDouble(item, "xMin", "x_min"));
            box.setYMin(readDouble(item, "yMin", "y_min"));
            box.setXMax(readDouble(item, "xMax", "x_max"));
            box.setYMax(readDouble(item, "yMax", "y_max"));
            boxes.add(box);
        }

        return boxes;
    }

    private String readText(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                String text = value.asText();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private Double readDouble(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value.asDouble();
            }
        }
        return null;
    }
}
