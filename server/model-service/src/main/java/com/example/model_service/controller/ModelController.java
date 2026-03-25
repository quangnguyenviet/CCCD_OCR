package com.example.model_service.controller;

import com.example.model_service.dto.ActiveModelsResponseDto;
import com.example.model_service.service.ModelBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Slf4j
public class ModelController {

    private final ModelBusinessService modelBusinessService;

    @GetMapping("/active")
    public ResponseEntity<ActiveModelsResponseDto> getActiveModels() {
        try {
            log.info("Request to get active models");
            ActiveModelsResponseDto response = modelBusinessService.getActiveModels();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving active models", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
