package com.example.training_service.service;

import com.example.training_service.client.DataServiceClient;
import com.example.training_service.client.DataServiceClient.BoundingBoxExportDto;
import com.example.training_service.client.DataServiceClient.DatasetExportDto;
import com.example.training_service.client.DataServiceClient.ImageExportDto;
import com.example.training_service.dto.training.TrainingRequestDto;
import com.example.training_service.dto.training.TrainingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final DateTimeFormatter FOLDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final double RATIO_TOLERANCE = 0.0001;
    private static volatile Process currentTrainingProcess = null;

    private final DataServiceClient dataServiceClient;

    @Value("${upload.dataset.dir}")
    private String datasetRootDir;

    @Value("${training.python.executable:python}")
    private String pythonExecutable;

    @Value("${training.script.path:train.py}")
    private String trainingScriptPath;

    public TrainingResponseDto startTraining(TrainingRequestDto request) throws IOException {
        validateRequest(request);

        String effectiveRunName = request.getName() == null || request.getName().isBlank()
            ? "cccd_yolo_run"
            : request.getName().trim();

        DatasetExportDto dataset = dataServiceClient.getDatasetExportDetails(request.getDatasetId());
        if (dataset == null) {
            throw new IllegalArgumentException("Không tìm thấy dataset với id=" + request.getDatasetId());
        }

        SplitResult splitResult = exportDataset(dataset, request);
        Path logFile = splitResult.exportDir.resolve("training.log");
        Files.createDirectories(splitResult.exportDir);
        if (!Files.exists(logFile)) {
            Files.createFile(logFile);
        }

        Path scriptPath = resolveScriptPath();

        List<String> command = new ArrayList<>();
        command.add(resolvePythonCommand());
        command.add(scriptPath.toString());
        command.add("--data");
        command.add(splitResult.dataYaml.toString());
        command.add("--epochs");
        command.add(String.valueOf(request.getEpochs()));
        command.add("--batch-size");
        command.add(String.valueOf(request.getBatchSize()));
        command.add("--imgsz");
        command.add(String.valueOf(request.getImageSize() == null ? 640 : request.getImageSize()));
        command.add("--weights");
        command.add("yolov8n.pt");
        command.add("--project");
        command.add(splitResult.modelOutputDir.toString());
        command.add("--name");
        command.add(effectiveRunName);
        command.add("--train-ratio");
        command.add(String.valueOf(request.getTrainRatio()));
        command.add("--val-ratio");
        command.add(String.valueOf(request.getValRatio()));
        command.add("--test-ratio");
        command.add(String.valueOf(request.getTestRatio()));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(scriptPath.getParent().toFile());
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
        Process process = processBuilder.start();
        currentTrainingProcess = process;
        process.onExit().thenRun(() -> currentTrainingProcess = null);

        return new TrainingResponseDto(
            "STARTED",
            "Đã khởi chạy huấn luyện.",
            dataset.datasetId(),
            dataset.datasetName(),
            splitResult.exportDir.toString(),
            logFile.toString(),
            splitResult.modelOutputDir.resolve(effectiveRunName).resolve("weights").resolve("best.pt").toString(),
            splitResult.dataYaml.toString(),
            splitResult.modelOutputDir.toString(),
            effectiveRunName,
            request.getEpochs(),
            request.getBatchSize(),
            request.getTrainRatio(),
            request.getValRatio(),
            request.getTestRatio()
        );
    }

    private SplitResult exportDataset(DatasetExportDto dataset, TrainingRequestDto request) throws IOException {
        List<ImageExportDto> images = dataset.images();
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("Dataset chưa có ảnh để huấn luyện.");
        }

        Set<String> classNamesSet = new LinkedHashSet<>();
        for (ImageExportDto image : images) {
            List<BoundingBoxExportDto> boxes = image.boxes();
            if (boxes == null || boxes.isEmpty()) {
                throw new IllegalArgumentException("Ảnh id=" + image.imageId() + " chưa có bounding box.");
            }
            for (BoundingBoxExportDto box : boxes) {
                classNamesSet.add(box.labelName());
            }
        }

        List<String> classNames = classNamesSet.stream().sorted().toList();
        Map<String, Integer> classIndexMap = new LinkedHashMap<>();
        for (int i = 0; i < classNames.size(); i++) {
            classIndexMap.put(classNames.get(i), i);
        }

        Path exportRoot = Paths.get(datasetRootDir, "training_exports");
        Files.createDirectories(exportRoot);

        String safeDatasetName = sanitizeFileName(dataset.datasetName());
        String folderName = safeDatasetName + "_" + dataset.datasetId() + "_" + LocalDateTime.now().format(FOLDER_TIME_FORMAT);
        Path exportDir = exportRoot.resolve(folderName);
        Path imagesTrainDir = exportDir.resolve("images/train");
        Path imagesValDir = exportDir.resolve("images/val");
        Path imagesTestDir = exportDir.resolve("images/test");
        Path labelsTrainDir = exportDir.resolve("labels/train");
        Path labelsValDir = exportDir.resolve("labels/val");
        Path labelsTestDir = exportDir.resolve("labels/test");

        Files.createDirectories(imagesTrainDir);
        Files.createDirectories(imagesValDir);
        Files.createDirectories(imagesTestDir);
        Files.createDirectories(labelsTrainDir);
        Files.createDirectories(labelsValDir);
        Files.createDirectories(labelsTestDir);

        List<ImageExportDto> shuffledImages = new ArrayList<>(images);
        Collections.shuffle(shuffledImages, new Random(42L));

        int total = shuffledImages.size();
        int trainCount = (int) Math.floor(total * request.getTrainRatio());
        int valCount = (int) Math.floor(total * request.getValRatio());
        int testCount = total - trainCount - valCount;
        if (testCount < 0) {
            testCount = 0;
            valCount = Math.max(0, total - trainCount);
        }

        List<ImageExportDto> trainImages = shuffledImages.subList(0, Math.min(trainCount, total));
        List<ImageExportDto> valImages = shuffledImages.subList(Math.min(trainCount, total), Math.min(trainCount + valCount, total));
        List<ImageExportDto> testImages = shuffledImages.subList(Math.min(trainCount + valCount, total), total);

        writeSplit(trainImages, imagesTrainDir, labelsTrainDir, classIndexMap);
        writeSplit(valImages, imagesValDir, labelsValDir, classIndexMap);
        writeSplit(testImages, imagesTestDir, labelsTestDir, classIndexMap);

        Path dataYaml = exportDir.resolve("data.yaml");
        writeDataYaml(dataYaml, exportDir, classNames);

        Path modelOutputDir = exportDir.resolve("runs");
        Files.createDirectories(modelOutputDir);

        return new SplitResult(exportDir, dataYaml, modelOutputDir);
    }

    private void writeSplit(
        List<ImageExportDto> images,
        Path targetImagesDir,
        Path targetLabelsDir,
        Map<String, Integer> classIndexMap
    ) throws IOException {
        for (ImageExportDto image : images) {
            Path source = Paths.get(image.imagePath());
            if (!Files.exists(source)) {
                throw new IllegalArgumentException("File ảnh không tồn tại: " + image.imagePath());
            }

            String fileName = buildOutputImageName(image);
            Path targetImage = targetImagesDir.resolve(fileName);
            Files.copy(source, targetImage, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            Path labelFile = targetLabelsDir.resolve(replaceExtension(fileName, ".txt"));
            List<String> lines = image.boxes().stream()
                .sorted(Comparator.comparing(BoundingBoxExportDto::boxId))
                .map(box -> toYoloLine(box, image, classIndexMap))
                .toList();
            Files.write(labelFile, lines, StandardCharsets.UTF_8);
        }
    }

    private String toYoloLine(BoundingBoxExportDto box, ImageExportDto image, Map<String, Integer> classIndexMap) {
        if (image.imageWidth() == null || image.imageHeight() == null || image.imageWidth() <= 0 || image.imageHeight() <= 0) {
            throw new IllegalArgumentException("Kích thước ảnh không hợp lệ cho image id=" + image.imageId());
        }

        Integer classIndex = classIndexMap.get(box.labelName());
        if (classIndex == null) {
            throw new IllegalArgumentException("Không tìm thấy class cho label: " + box.labelName());
        }

        double xCenter = ((box.xMin() + box.xMax()) / 2.0) / image.imageWidth();
        double yCenter = ((box.yMin() + box.yMax()) / 2.0) / image.imageHeight();
        double width = (box.xMax() - box.xMin()) / image.imageWidth();
        double height = (box.yMax() - box.yMin()) / image.imageHeight();

        return String.format(Locale.US, "%d %.6f %.6f %.6f %.6f", classIndex, xCenter, yCenter, width, height);
    }

    private void writeDataYaml(Path dataYaml, Path exportDir, List<String> classNames) throws IOException {
        String namesBlock = classNames.stream()
            .map(name -> "  - " + name)
            .collect(Collectors.joining(System.lineSeparator()));

        String content = String.join(System.lineSeparator(),
            "path: " + toForwardSlash(exportDir.toAbsolutePath().toString()),
            "train: images/train",
            "val: images/val",
            "test: images/test",
            "nc: " + classNames.size(),
            "names:",
            namesBlock
        ) + System.lineSeparator();

        Files.writeString(dataYaml, content, StandardCharsets.UTF_8);
    }

    private void validateRequest(TrainingRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request không hợp lệ.");
        }
        if (request.getDatasetId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn dataset.");
        }
        if (request.getEpochs() == null || request.getEpochs() <= 0) {
            throw new IllegalArgumentException("Epoch phải lớn hơn 0.");
        }
        if (request.getBatchSize() == null || request.getBatchSize() <= 0) {
            throw new IllegalArgumentException("Batch size phải lớn hơn 0.");
        }
        if (request.getTrainRatio() == null || request.getValRatio() == null || request.getTestRatio() == null) {
            throw new IllegalArgumentException("Tỷ lệ train/val/test không được để trống.");
        }

        double sum = request.getTrainRatio() + request.getValRatio() + request.getTestRatio();
        if (Math.abs(sum - 1.0) > RATIO_TOLERANCE) {
            throw new IllegalArgumentException("Tỷ lệ train + val + test phải bằng 1.0.");
        }
    }

    private Path resolveScriptPath() {
        Path scriptPath = Paths.get(trainingScriptPath);
        if (!scriptPath.isAbsolute()) {
            scriptPath = Paths.get(System.getProperty("user.dir")).resolve(scriptPath).normalize();
        }
        if (!Files.exists(scriptPath)) {
            throw new IllegalArgumentException("Không tìm thấy file train.py tại: " + scriptPath);
        }
        return scriptPath;
    }

    private String resolvePythonCommand() {
        return pythonExecutable == null || pythonExecutable.isBlank() ? "python" : pythonExecutable;
    }

    private String buildOutputImageName(ImageExportDto image) {
        String baseName = replaceExtension(sanitizeFileName(image.originalFilename()), getFileExtension(image.imagePath()));
        if (baseName.isBlank()) {
            baseName = "image_" + image.imageId() + getFileExtension(image.imagePath());
        }
        return image.imageId() + "_" + baseName;
    }

    private String replaceExtension(String fileName, String newExtension) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0) {
            return fileName.substring(0, lastDot) + newExtension;
        }
        return fileName + newExtension;
    }

    private String getFileExtension(String filePath) {
        if (filePath == null) {
            return ".jpg";
        }
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot < 0) {
            return ".jpg";
        }
        return filePath.substring(lastDot);
    }

    private String sanitizeFileName(String value) {
        if (value == null) {
            return "dataset";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String toForwardSlash(String value) {
        return value.replace("\\", "/");
    }

    public void stopTraining() {
        if (currentTrainingProcess != null && currentTrainingProcess.isAlive()) {
            currentTrainingProcess.destroyForcibly();
            currentTrainingProcess = null;
        }
    }

    public boolean isTrainingRunning() {
        return currentTrainingProcess != null && currentTrainingProcess.isAlive();
    }

    private record SplitResult(Path exportDir, Path dataYaml, Path modelOutputDir) {
    }
}
