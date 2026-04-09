package com.example.model_service.controller;

import com.example.model_service.dto.dataset.ImageOptionDto;
import com.example.model_service.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ImageController {

    private final ImageService imageService;

    @GetMapping
    public ResponseEntity<List<ImageOptionDto>> listImages() {
        return ResponseEntity.ok(imageService.listImages());
    }

    @GetMapping("/{imageId}/preview")
    public ResponseEntity<?> previewImage(@PathVariable Long imageId) {
        try {
            Resource resource = imageService.loadImageResource(imageId);
            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            log.error("Preview image failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể đọc file ảnh."));
        } catch (Exception e) {
            log.error("Preview image failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể tải ảnh preview."));
        }
    }
}
