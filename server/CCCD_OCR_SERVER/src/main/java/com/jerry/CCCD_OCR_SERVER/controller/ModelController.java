package com.jerry.CCCD_OCR_SERVER.controller;

import com.jerry.CCCD_OCR_SERVER.dto.ActiveModelsResponseDto;
import com.jerry.CCCD_OCR_SERVER.service.ModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller xử lý các request liên quan đến mô hình AI
 */
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Slf4j
public class ModelController {
    
    private final ModelService modelService;
    
    /**
     * Lấy danh sách tất cả mô hình hoạt động
     * @return ResponseEntity chứa danh sách mô hình được phân loại theo loại
     */
    @GetMapping("/active")
    public ResponseEntity<ActiveModelsResponseDto> getActiveModels() {
        try {
            log.info("Request to get active models");
            ActiveModelsResponseDto response = modelService.getActiveModels();
            log.info("Successfully retrieved active models");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving active models", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
