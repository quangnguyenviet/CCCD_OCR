package com.example.model_service.controller;

import com.example.model_service.service.ExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/extract")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExtractionController {

    private final ExtractionService extractionService;

    @PostMapping("/image")
    public ResponseEntity<?> extractFromImage(
            @RequestParam("image_file") MultipartFile imageFile,
            @RequestParam(value = "card_model_url", required = false) String cardModelUrl,
            @RequestParam(value = "roi_model_url", required = false) String roiModelUrl,
            @RequestParam(value = "ocr_model_url", required = false) String ocrModelUrl) {
        try {
            log.info("Received extraction request for image: {}, cardModel: {}, roiModel: {}, ocrModel: {}", 
                     imageFile.getOriginalFilename(), cardModelUrl, roiModelUrl, ocrModelUrl);
            
            Map<String, Object> result = extractionService.extract(imageFile, cardModelUrl, roiModelUrl, ocrModelUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Extraction failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Thất bại khi thực thi script Python trích xuất hình ảnh: " + e.getMessage()));
        }
    }
}
