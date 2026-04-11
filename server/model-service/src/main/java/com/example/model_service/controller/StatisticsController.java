package com.example.model_service.controller;

import com.example.model_service.dto.statistics.ModelStatisticsDto;
import com.example.model_service.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/models")
    public ResponseEntity<ModelStatisticsDto> getModelStatistics() {
        try {
            ModelStatisticsDto stats = statisticsService.getModelStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting model statistics", e);
            throw e;
        }
    }
}
