package com.example.model_service.service;

import com.example.model_service.entity.Model;
import com.example.model_service.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingMetricsService {

    private final ModelRepository modelRepository;

    /**
     * Parse training log file and extract metrics
     */
    public TrainingMetrics parseTrainingLog(String logFilePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(logFilePath), StandardCharsets.UTF_8);
            return parseMetricsFromLines(lines);
        } catch (IOException e) {
            return new TrainingMetrics(null, null);
        }
    }

    /**
     * Save training metrics to model in database
     */
    @Transactional
    public void saveTrainingMetrics(Long modelId, LocalDateTime startTime, String logFilePath) {
        Optional<Model> optModel = modelRepository.findById(modelId);
        if (optModel.isEmpty()) {
            return;
        }

        Model model = optModel.get();
        
        // Set training start and end times
        if (model.getTrainingStartTime() == null) {
            model.setTrainingStartTime(startTime);
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        model.setTrainingEndTime(endTime);
        
        // Calculate duration in seconds
        if (model.getTrainingStartTime() != null) {
            long durationSeconds = java.time.temporal.ChronoUnit.SECONDS
                .between(model.getTrainingStartTime(), endTime);
            model.setTrainingDurationSeconds((int) durationSeconds);
        }
        
        // Parse metrics from log file
        TrainingMetrics metrics = parseTrainingLog(logFilePath);
        if (metrics.finalLoss != null) {
            model.setFinalLoss(metrics.finalLoss);
        }
        if (metrics.metricsJson != null) {
            model.setFinalMetrics(metrics.metricsJson);
        }
        
        // Update status to COMPLETED
        model.setStatus("COMPLETED");
        model.setProgressPercent(100);
        
        modelRepository.save(model);
    }

    private TrainingMetrics parseMetricsFromLines(List<String> lines) {
        Double finalLoss = null;
        Map<String, Double> metrics = new HashMap<>();
        
        // Look for final loss in training output
        // YOLOv8 format: "Class     Images     Labels  Box(P          R      mAP50  mAP5095): 0.xx"
        // or "val/loss: 0.xxx"
        Pattern lossPattern = Pattern.compile("(?:val/loss|box_loss|cls_loss|dfl_loss)\\s*[:\\s]+([\\d.]+)");
        Pattern metricsPattern = Pattern.compile("(mAP50|Recall|Precision|mAP)\\s*[:\\s]+([\\d.]+)");
        
        String lastLine = "";
        for (String line : lines) {
            lastLine = line;
            
            // Extract loss values
            Matcher lossMatcher = lossPattern.matcher(line);
            while (lossMatcher.find()) {
                try {
                    double loss = Double.parseDouble(lossMatcher.group(1));
                    if (loss < 100) { // Sanity check
                        finalLoss = loss;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            
            // Extract other metrics (mAP, Recall, Precision, etc.)
            Matcher metricsMatcher = metricsPattern.matcher(line);
            while (metricsMatcher.find()) {
                try {
                    String metricName = metricsMatcher.group(1);
                    double value = Double.parseDouble(metricsMatcher.group(2));
                    metrics.put(metricName, value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        
        // If no loss found in metrics, try to parse final epoch line
        if (finalLoss == null && !lastLine.isEmpty()) {
            // YOLOv8 epoch line format: "Epoch   gpu_mem      box      obj      cls    labels  img_size"
            Pattern epochPattern = Pattern.compile("\\d+/\\d+\\s+[\\d.]+G\\s+([\\d.]+)\\s+");
            Matcher epochMatcher = epochPattern.matcher(lastLine);
            if (epochMatcher.find()) {
                try {
                    finalLoss = Double.parseDouble(epochMatcher.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        
        // Convert metrics to JSON string
        String metricsJson = null;
        if (!metrics.isEmpty()) {
            metricsJson = convertMetricsToJson(metrics);
        }
        
        return new TrainingMetrics(finalLoss, metricsJson);
    }

    private String convertMetricsToJson(Map<String, Double> metrics) {
        StringBuilder json = new StringBuilder("{");
        json.append(metrics.entrySet().stream()
            .map(entry -> "\"" + entry.getKey() + "\":" + String.format("%.4f", entry.getValue()))
            .collect(Collectors.joining(",")));
        json.append("}");
        return json.toString();
    }

    public static class TrainingMetrics {
        public Double finalLoss;
        public String metricsJson;

        public TrainingMetrics(Double finalLoss, String metricsJson) {
            this.finalLoss = finalLoss;
            this.metricsJson = metricsJson;
        }
    }
}
