package com.example.training_service.service;

import com.example.training_service.dto.training.TrainingLogResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class TrainingLogService {

    @Value("${upload.dataset.dir}")
    private String datasetRootDir;

    public TrainingLogResponseDto readLog(String logFilePath, Integer tailLines) throws IOException {
        Path path = resolveSafePath(logFilePath);
        if (!Files.exists(path)) {
            return new TrainingLogResponseDto(path.toString(), false, "");
        }

        int lines = tailLines == null || tailLines <= 0 ? 200 : tailLines;
        List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        int start = Math.max(0, allLines.size() - lines);
        String content = String.join(System.lineSeparator(), allLines.subList(start, allLines.size()));
        return new TrainingLogResponseDto(path.toString(), true, content);
    }

    private Path resolveSafePath(String logFilePath) {
        if (logFilePath == null || logFilePath.isBlank()) {
            throw new IllegalArgumentException("logFilePath không hợp lệ.");
        }

        Path baseDir = Paths.get(datasetRootDir, "training_exports").toAbsolutePath().normalize();
        Path target = Paths.get(logFilePath).toAbsolutePath().normalize();

        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Đường dẫn log không hợp lệ.");
        }

        return target;
    }
}
