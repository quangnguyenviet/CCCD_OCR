package com.example.model_service.controller;

import com.example.model_service.dto.dataset.DatasetOptionDto;
import com.example.model_service.dto.dataset.CreateDatasetRequestDto;
import com.example.model_service.dto.dataset.CreateDatasetResponseDto;
import com.example.model_service.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DatasetController {

    private final DatasetService datasetService;

    @GetMapping
    public ResponseEntity<List<DatasetOptionDto>> listDatasets() {
        return ResponseEntity.ok(datasetService.listDatasets());
    }

    @PostMapping
    public ResponseEntity<?> createDataset(@RequestBody CreateDatasetRequestDto request) {
        try {
            CreateDatasetResponseDto response = datasetService.createDataset(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Create dataset failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể tạo dataset."));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
