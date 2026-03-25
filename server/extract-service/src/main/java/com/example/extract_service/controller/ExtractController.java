package com.example.extract_service.controller;

import com.example.extract_service.dto.ExtractImageResponseDto;
import com.example.extract_service.service.ExtractImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/extract")
@RequiredArgsConstructor
@Slf4j
public class ExtractController {

    private final ExtractImageService extractImageService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExtractImageResponseDto> extractImage(
        @RequestParam("image_file") MultipartFile imageFile,
        @RequestParam("card_model_url") String cardModelUrl,
        @RequestParam("roi_model_url") String roiModelUrl,
        @RequestParam("ocr_model_url") String ocrModelUrl
    ) {
        log.info("Received extract image request: cardModelUrl={}, roiModelUrl={}, ocrModelUrl={}",
            cardModelUrl, roiModelUrl, ocrModelUrl);

        ExtractImageResponseDto response = extractImageService.extract(
            imageFile,
            cardModelUrl,
            roiModelUrl,
            ocrModelUrl
        );

        return ResponseEntity.ok(response);
    }
}
