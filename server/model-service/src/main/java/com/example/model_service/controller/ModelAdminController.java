package com.example.model_service.controller;

import com.example.model_service.dto.model.RegisterTrainedModelRequestDto;
import com.example.model_service.dto.model.RegisterTrainedModelResponseDto;
import com.example.model_service.dto.model.ModelMetricsDto;
import com.example.model_service.repository.ModelRepository;
import com.example.model_service.service.ModelRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ModelRepository modelRepository;

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

    @GetMapping("/{id}/metrics")
    public ResponseEntity<?> getModelMetrics(@PathVariable Long id) {
        try {
            return modelRepository.findById(id)
                .map(model -> {
                    ModelMetricsDto dto = new ModelMetricsDto();
                    dto.setId(model.getId());
                    dto.setName(model.getName());
                    dto.setType(model.getType().name());
                    dto.setEpochs(model.getEpochs());
                    dto.setBatchSize(model.getBatchSize());
                    dto.setStatus(model.getStatus());
                    dto.setTrainingStartTime(model.getTrainingStartTime());
                    dto.setTrainingEndTime(model.getTrainingEndTime());
                    dto.setTrainingDurationSeconds(model.getTrainingDurationSeconds());
                    dto.setFinalLoss(model.getFinalLoss());
                    dto.setFinalMetrics(model.getFinalMetrics());
                    dto.setCreatedAt(model.getCreatedAt());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Get model metrics failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể lấy metrics của model."));
        }
    }
}
