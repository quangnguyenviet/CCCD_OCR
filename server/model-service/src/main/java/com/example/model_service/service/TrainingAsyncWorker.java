package com.example.model_service.service;

import com.example.model_service.entity.AiModel;
import com.example.model_service.repository.AiModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class TrainingAsyncWorker {

    @Autowired
    private AiModelRepository repository;

    @Value("${training.python.executable:python}")
    private String pythonExecutable;

    @Value("${training.script.path:train.py}")
    private String trainingScriptPath;

    private final Map<Long, Process> runningProcesses = new ConcurrentHashMap<>();

    public boolean requestStop(Long modelId) {
        Process process = runningProcesses.get(modelId);
        if (process == null || !process.isAlive()) {
            return false;
        }

        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }

        return true;
    }

    // Annotation @Async giúp hàm này chạy trên một Thread riêng biệt (Worker Thread)
    @Async
    public void processZipAndTrain(Long modelId, String filePath, int epochs, int batchSize) {
        AiModel model = repository.findById(modelId).orElseThrow();
        Path logFilePath = null;

        if (model.getStatus() == AiModel.Status.STOPPED) {
            updateStatus(model, AiModel.Status.STOPPED, model.getProgressPercent(), "[INFO] Job đã được dừng trước khi khởi chạy.");
            return;
        }

        try {
            updateStatus(model, AiModel.Status.EXTRACTING, 5, "[INFO] Chuẩn bị chạy tiến trình huấn luyện từ file: " + filePath);
            Path scriptPath = resolveTrainingScriptPath();

            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add("-u");
            command.add(scriptPath.toString());
            command.add("--zip");
            command.add(filePath);
            command.add("--epochs");
            command.add(String.valueOf(epochs));
            command.add("--batch");
            command.add(String.valueOf(batchSize));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(scriptPath.getParent().toFile());
            processBuilder.redirectErrorStream(true);
            processBuilder.environment().put("PYTHONUNBUFFERED", "1");

            Path logsDir = Paths.get(System.getProperty("java.io.tmpdir"), "training-logs");
            Files.createDirectories(logsDir);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            logFilePath = logsDir.resolve("training_" + modelId + "_" + timestamp + ".log");

            updateStatus(model, AiModel.Status.TRAINING, 10, "[INFO] Đã khởi chạy lệnh: " + String.join(" ", command));
            updateStatus(model, AiModel.Status.TRAINING, 10, "[INFO] Log file: " + logFilePath);

            Process process = processBuilder.start();
            runningProcesses.put(modelId, process);

            try (BufferedWriter logWriter = Files.newBufferedWriter(
                    logFilePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                writeLogLine(logWriter, "[INFO] Started at: " + LocalDateTime.now());
                writeLogLine(logWriter, "[INFO] Model ID: " + modelId);
                writeLogLine(logWriter, "[INFO] Command: " + String.join(" ", command));
                writeLogLine(logWriter, "[INFO] Working dir: " + scriptPath.getParent());
                writeLogLine(logWriter, "[INFO] Dataset ZIP: " + filePath);

                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    writeLogLine(logWriter, line);

                    if (trimmed.startsWith("PROGRESS=")) {
                        try {
                            int progress = Integer.parseInt(trimmed.substring("PROGRESS=".length()));
                            int bounded = Math.max(10, Math.min(progress, 99));
                            updateStatus(model, AiModel.Status.TRAINING, bounded, "[TRAIN] " + trimmed);
                        } catch (NumberFormatException ignored) {
                            updateStatus(model, AiModel.Status.TRAINING, model.getProgressPercent(), "[TRAIN] " + trimmed);
                        }
                    } else if (trimmed.startsWith("MODEL_PATH=")) {
                        String modelPath = trimmed.substring("MODEL_PATH=".length());
                        model.setModelFilePath(modelPath);
                        repository.save(model);
                        updateStatus(model, AiModel.Status.TRAINING, Math.max(model.getProgressPercent(), 95), "[INFO] Đã nhận đường dẫn model: " + modelPath);
                    } else {
                        updateStatus(model, AiModel.Status.TRAINING, model.getProgressPercent(), "[TRAIN] " + trimmed);
                    }
                }
            }

            int exitCode = process.waitFor();
            AiModel latest = repository.findById(modelId).orElseThrow();
            if (latest.getStatus() == AiModel.Status.STOPPED) {
                latest.setLatestLog("[STOPPED] Tiến trình huấn luyện đã được dừng thủ công. Log file: " + logFilePath);
                repository.save(latest);
            } else if (exitCode == 0) {
                model.setStatus(AiModel.Status.SUCCESS);
                model.setProgressPercent(100);
                model.setLatestLog("[SUCCESS] Huấn luyện hoàn tất. Exited with code: 0. Log file: " + logFilePath);
                repository.save(model);
            } else {
                updateStatus(model, AiModel.Status.FAILED, model.getProgressPercent(), "[ERROR] Tiến trình train thất bại. Exited with code: " + exitCode + ". Log file: " + logFilePath);
            }

        } catch (Exception e) {
            AiModel latest = repository.findById(modelId).orElse(model);
            if (latest.getStatus() == AiModel.Status.STOPPED) {
                updateStatus(latest, AiModel.Status.STOPPED, latest.getProgressPercent(), "[STOPPED] Tiến trình huấn luyện đã được dừng thủ công. Log file: " + logFilePath);
            } else {
                // Bắt lỗi và cập nhật trạng thái FAILED
                updateStatus(model, AiModel.Status.FAILED, model.getProgressPercent(), "[ERROR] Tiến trình thất bại: " + e.getMessage() + ". Log file: " + logFilePath);
            }
        } finally {
            runningProcesses.remove(modelId);
        }
    }

    private void writeLogLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line != null ? line : "");
        writer.newLine();
        writer.flush();
    }

    private Path resolveTrainingScriptPath() {
        Path configured = Paths.get(trainingScriptPath);
        if (configured.isAbsolute() && Files.isRegularFile(configured)) {
            return configured.normalize();
        }

        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path candidateInModelService = cwd.resolve("model-service").resolve(trainingScriptPath).normalize();
        Path candidateInCwd = cwd.resolve(trainingScriptPath).normalize();

        Path resolved = Stream.of(candidateInModelService, candidateInCwd)
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy training script: " + trainingScriptPath + " (cwd=" + cwd + ")"));

        return resolved;
    }

    // Hàm phụ trợ để ghi log và % vào DB liên tục
    private void updateStatus(AiModel model, AiModel.Status status, int percent, String log) {
        model.setStatus(status);
        model.setProgressPercent(percent);
        model.setLatestLog(log);
        repository.save(model);
        System.out.println(log); // In ra console của server để dễ debug
    }
}
