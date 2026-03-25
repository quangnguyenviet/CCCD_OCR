package com.jerry.CCCD_OCR_SERVER.controller;

import com.jerry.CCCD_OCR_SERVER.dto.ExtractImageResponseDto;
import com.jerry.CCCD_OCR_SERVER.service.ExtractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    private final ExtractService extractService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExtractImageResponseDto> extractFromImage(
        @RequestParam("image_file") MultipartFile imageFile,
        @RequestParam("card_model_id") String cardModelId,
        @RequestParam("roi_model_id") String roiModelId,
        @RequestParam("ocr_model_id") String ocrModelId
    ) {
        if (imageFile == null || imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(
                ExtractImageResponseDto.builder()
                    .status("error")
                    .message("image_file là bắt buộc")
                    .processingTimeMs(0L)
                    .data(null)
                    .build()
            );
        }

        String contentType = imageFile.getContentType();
        boolean supportedType = MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)
            || MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)
            || "image/jpg".equalsIgnoreCase(contentType);

        if (!supportedType) {
            return ResponseEntity.badRequest().body(
                ExtractImageResponseDto.builder()
                    .status("error")
                    .message("Chỉ hỗ trợ file JPG/PNG")
                    .processingTimeMs(0L)
                    .data(null)
                    .build()
            );
        }

        try {
            ExtractImageResponseDto response = extractService.extractFromImage(
                imageFile,
                cardModelId,
                roiModelId,
                ocrModelId
            );
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error while extracting info from image", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ExtractImageResponseDto.builder()
                    .status("error")
                    .message("Có lỗi xảy ra khi trích xuất thông tin")
                    .processingTimeMs(0L)
                    .data(null)
                    .build()
            );
        }
    }
}
