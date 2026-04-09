package com.example.model_service.controller;

import com.example.model_service.dto.model.RegisterTrainedModelRequestDto;
import com.example.model_service.dto.model.RegisterTrainedModelResponseDto;
import com.example.model_service.service.ModelRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ModelAdminController {

    private final ModelRegistrationService modelRegistrationService;

    @PostMapping("/register-trained")
    public ResponseEntity<?> registerTrainedModel(@RequestBody RegisterTrainedModelRequestDto request) {
        try {
            RegisterTrainedModelResponseDto response = modelRegistrationService.registerTrainedModel(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Register trained model failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể lưu model huấn luyện."));
        }
    }
}
