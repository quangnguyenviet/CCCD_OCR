package com.example.model_service.controller;

import com.example.model_service.dto.training.TrainingRequestDto;
import com.example.model_service.dto.training.TrainingResponseDto;
import com.example.model_service.service.TrainingService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping("/start")
    public ResponseEntity<?> startTraining(@RequestBody TrainingRequestDto request) {
        try {
            TrainingResponseDto response = trainingService.startTraining(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Start training failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể khởi chạy huấn luyện."));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopTraining() {
        try {
            trainingService.stopTraining();
            return ResponseEntity.ok(Map.of("message", "Đã dừng huấn luyện."));
        } catch (Exception e) {
            log.error("Stop training failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể dừng huấn luyện."));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTrainingStatus() {
        return ResponseEntity.ok(Map.of("running", trainingService.isTrainingRunning()));
    }
}
