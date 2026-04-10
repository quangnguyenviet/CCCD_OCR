package com.example.model_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.model_service.entity.Model;
import com.example.model_service.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingMetricsService {

    private static final String TRAINING_SUMMARY_PREFIX = "TRAINING_SUMMARY_JSON:";

    private final ModelRepository modelRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public TrainingMetrics parseTrainingArtifacts(String logFilePath, String modelFilePath) {
        TrainingMetrics csvMetrics = parseMetricsFromResultsCsv(modelFilePath);
        if (csvMetrics.finalLoss != null || csvMetrics.metricsJson != null) {
            return csvMetrics;
        }
        return parseTrainingLog(logFilePath);
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

        TrainingSummaryFromLog summary = parseTrainingSummaryFromLog(logFilePath);

        LocalDateTime inferredStart = inferTrainingStartTime(logFilePath);
        LocalDateTime inferredEnd = inferTrainingEndTime(logFilePath);
        LocalDateTime effectiveStart = summary.startTime != null
            ? summary.startTime
            : (inferredStart != null
            ? inferredStart
            : (startTime != null ? startTime : LocalDateTime.now()));
        LocalDateTime effectiveEnd = summary.endTime != null
            ? summary.endTime
            : (inferredEnd != null ? inferredEnd : LocalDateTime.now());

        model.setTrainingStartTime(effectiveStart);
        model.setTrainingEndTime(effectiveEnd);

        // Calculate duration in seconds
        if (summary.durationSeconds != null) {
            model.setTrainingDurationSeconds(Math.max(0, summary.durationSeconds));
        } else if (effectiveStart != null && effectiveEnd != null) {
            long durationSeconds = java.time.temporal.ChronoUnit.SECONDS
                .between(effectiveStart, effectiveEnd);
            model.setTrainingDurationSeconds((int) Math.max(0, durationSeconds));
        }

        // Parse metrics from log file
        TrainingMetrics metrics;
        if (summary.finalLoss != null || summary.metricsJson != null) {
            metrics = new TrainingMetrics(summary.finalLoss, summary.metricsJson);
        } else {
            metrics = parseTrainingArtifacts(logFilePath, model.getModelFilePath());
        }
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

    private TrainingMetrics parseMetricsFromResultsCsv(String modelFilePath) {
        if (modelFilePath == null || modelFilePath.isBlank()) {
            return new TrainingMetrics(null, null);
        }

        try {
            Path modelPath = Paths.get(modelFilePath);
            Path runDir = modelPath.getParent() != null ? modelPath.getParent().getParent() : null; // .../weights/best.pt -> run dir
            if (runDir == null) {
                return new TrainingMetrics(null, null);
            }

            Path resultsCsv = runDir.resolve("results.csv");
            if (!Files.exists(resultsCsv)) {
                return new TrainingMetrics(null, null);
            }

            List<String> lines = Files.readAllLines(resultsCsv, StandardCharsets.UTF_8).stream()
                .filter(line -> line != null && !line.isBlank())
                .toList();

            if (lines.size() < 2) {
                return new TrainingMetrics(null, null);
            }

            String[] headers = lines.get(0).split(",");
            String[] values = lines.get(lines.size() - 1).split(",");
            int size = Math.min(headers.length, values.length);

            Map<String, Double> parsed = new LinkedHashMap<>();
            for (int i = 0; i < size; i++) {
                String key = headers[i].trim();
                String value = values[i].trim();
                try {
                    parsed.put(key, Double.parseDouble(value));
                } catch (NumberFormatException ignored) {
                }
            }

            Double valBox = firstOf(parsed, "val/box_loss");
            Double valCls = firstOf(parsed, "val/cls_loss");
            Double valDfl = firstOf(parsed, "val/dfl_loss");
            Double trainBox = firstOf(parsed, "train/box_loss");
            Double trainCls = firstOf(parsed, "train/cls_loss");
            Double trainDfl = firstOf(parsed, "train/dfl_loss");

            Double finalLoss = null;
            if (valBox != null || valCls != null || valDfl != null) {
                finalLoss = safe(valBox) + safe(valCls) + safe(valDfl);
            } else if (trainBox != null || trainCls != null || trainDfl != null) {
                finalLoss = safe(trainBox) + safe(trainCls) + safe(trainDfl);
            }

            Map<String, Double> metrics = new LinkedHashMap<>();
            putIfPresent(metrics, "precision", firstOf(parsed, "metrics/precision(B)", "metrics/precision"));
            putIfPresent(metrics, "recall", firstOf(parsed, "metrics/recall(B)", "metrics/recall"));
            putIfPresent(metrics, "mAP50", firstOf(parsed, "metrics/mAP50(B)", "metrics/mAP50"));
            putIfPresent(metrics, "mAP50-95", firstOf(parsed, "metrics/mAP50-95(B)", "metrics/mAP50-95"));
            putIfPresent(metrics, "fitness", firstOf(parsed, "fitness"));

            String metricsJson = metrics.isEmpty() ? null : convertMetricsToJson(metrics);
            return new TrainingMetrics(finalLoss, metricsJson);
        } catch (Exception e) {
            return new TrainingMetrics(null, null);
        }
    }

    private TrainingMetrics parseMetricsFromLines(List<String> lines) {
        Double finalLoss = null;
        Map<String, Double> metrics = new HashMap<>();
        
        // Look for final loss in training output
        // YOLOv8 format: "Class     Images     Labels  Box(P          R      mAP50  mAP5095): 0.xx"
        // or "val/loss: 0.xxx"
        Pattern lossPattern = Pattern.compile("(?:val/loss|box_loss|cls_loss|dfl_loss)\\s*[:=\\s]+([\\d.]+)", Pattern.CASE_INSENSITIVE);
        Pattern metricsPattern = Pattern.compile("(mAP50-?95|mAP50|Recall|Precision|mAP)\\s*[:=\\s]+([\\d.]+)", Pattern.CASE_INSENSITIVE);
        
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
            .map(entry -> "\"" + entry.getKey() + "\":" + String.format(Locale.US, "%.4f", entry.getValue()))
            .collect(Collectors.joining(",")));
        json.append("}");
        return json.toString();
    }

    private LocalDateTime inferTrainingStartTime(String logFilePath) {
        if (logFilePath == null || logFilePath.isBlank()) {
            return null;
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(Paths.get(logFilePath), BasicFileAttributes.class);
            return LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDateTime inferTrainingEndTime(String logFilePath) {
        if (logFilePath == null || logFilePath.isBlank()) {
            return null;
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(Paths.get(logFilePath), BasicFileAttributes.class);
            return LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Double firstOf(Map<String, Double> source, String... keys) {
        for (String key : keys) {
            Double value = source.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static void putIfPresent(Map<String, Double> target, String key, Double value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private static double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private TrainingSummaryFromLog parseTrainingSummaryFromLog(String logFilePath) {
        if (logFilePath == null || logFilePath.isBlank()) {
            return TrainingSummaryFromLog.empty();
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(logFilePath), StandardCharsets.UTF_8);
            for (int i = lines.size() - 1; i >= 0; i--) {
                String line = lines.get(i);
                if (line == null) {
                    continue;
                }
                String trimmed = line.trim();
                if (!trimmed.startsWith(TRAINING_SUMMARY_PREFIX)) {
                    continue;
                }

                String json = trimmed.substring(TRAINING_SUMMARY_PREFIX.length()).trim();
                if (json.isEmpty()) {
                    continue;
                }

                Map<String, Object> payload = objectMapper.readValue(json, new TypeReference<>() {
                });

                LocalDateTime start = parseDateTime(payload.get("training_start_time"));
                LocalDateTime end = parseDateTime(payload.get("training_end_time"));
                Integer duration = parseInteger(payload.get("duration_seconds"));
                Double finalLoss = parseDouble(payload.get("final_loss"));

                String metricsJson = null;
                Object metricsObj = payload.get("metrics");
                if (metricsObj instanceof Map<?, ?> map && !map.isEmpty()) {
                    Map<String, Double> normalized = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() == null) {
                            continue;
                        }
                        Double value = parseDouble(entry.getValue());
                        if (value != null) {
                            normalized.put(String.valueOf(entry.getKey()), value);
                        }
                    }
                    if (!normalized.isEmpty()) {
                        metricsJson = convertMetricsToJson(normalized);
                    }
                }

                return new TrainingSummaryFromLog(start, end, duration, finalLoss, metricsJson);
            }
        } catch (Exception ignored) {
            return TrainingSummaryFromLog.empty();
        }

        return TrainingSummaryFromLog.empty();
    }

    private LocalDateTime parseDateTime(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(String.valueOf(raw)).toLocalDateTime();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Double parseDouble(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseInteger(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class TrainingMetrics {
        public Double finalLoss;
        public String metricsJson;

        public TrainingMetrics(Double finalLoss, String metricsJson) {
            this.finalLoss = finalLoss;
            this.metricsJson = metricsJson;
        }
    }

    private static class TrainingSummaryFromLog {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final Integer durationSeconds;
        private final Double finalLoss;
        private final String metricsJson;

        private TrainingSummaryFromLog(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer durationSeconds,
            Double finalLoss,
            String metricsJson
        ) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationSeconds = durationSeconds;
            this.finalLoss = finalLoss;
            this.metricsJson = metricsJson;
        }

        private static TrainingSummaryFromLog empty() {
            return new TrainingSummaryFromLog(null, null, null, null, null);
        }
    }
}
