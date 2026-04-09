package com.example.model_service.controller;

import com.example.model_service.dto.training.TrainingLogResponseDto;
import com.example.model_service.service.TrainingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TrainingLogController {

    private final TrainingLogService trainingLogService;

    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(
        @RequestParam("logFilePath") String logFilePath,
        @RequestParam(value = "tailLines", required = false) Integer tailLines
    ) {
        try {
            TrainingLogResponseDto response = trainingLogService.readLog(logFilePath, tailLines);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            log.error("Read training logs failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Không thể đọc log huấn luyện."));
        }
    }
}
