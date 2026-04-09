package com.example.model_service.service;

import com.example.model_service.dto.training.TrainingRequestDto;
import com.example.model_service.dto.training.TrainingResponseDto;
import com.example.model_service.entity.BoundingBox;
import com.example.model_service.entity.DatasetEntity;
import com.example.model_service.entity.DatasetItemEntity;
import com.example.model_service.entity.ImageEntity;
import com.example.model_service.repository.BoundingBoxRepository;
import com.example.model_service.repository.DatasetItemRepository;
import com.example.model_service.repository.DatasetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final DatasetRepository datasetRepository;
    private final DatasetItemRepository datasetItemRepository;
    private final BoundingBoxRepository boundingBoxRepository;

    @Value("${upload.dataset.dir}")
    private String datasetRootDir;

    @Value("${training.python.executable:python}")
    private String pythonExecutable;

    @Value("${training.script.path:train.py}")
    private String trainingScriptPath;

    @Transactional
    public TrainingResponseDto startTraining(TrainingRequestDto request) throws IOException {
        validateRequest(request);

        String effectiveRunName = request.getRunName() == null || request.getRunName().isBlank()
            ? "cccd_yolo_run"
            : request.getRunName().trim();

        DatasetEntity dataset = datasetRepository.findById(request.getDatasetId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dataset với id=" + request.getDatasetId()));

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
        command.add(request.getBaseWeights() == null || request.getBaseWeights().isBlank() ? "yolov8n.pt" : request.getBaseWeights());
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
        processBuilder.start();

        return new TrainingResponseDto(
            "STARTED",
            "Đã khởi chạy huấn luyện.",
            dataset.getId(),
            dataset.getDatasetName(),
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

    private SplitResult exportDataset(DatasetEntity dataset, TrainingRequestDto request) throws IOException {
        List<DatasetItemEntity> datasetItems = datasetItemRepository.findByDatasetId(dataset.getId());
        if (datasetItems.isEmpty()) {
            throw new IllegalArgumentException("Dataset chưa có ảnh để huấn luyện.");
        }

        List<ImageEntity> images = datasetItems.stream()
            .map(DatasetItemEntity::getImage)
            .distinct()
            .sorted(Comparator.comparing(ImageEntity::getId))
            .toList();

        Map<Long, List<BoundingBox>> boxesByImageId = new LinkedHashMap<>();
        Set<String> classNamesSet = new LinkedHashSet<>();
        for (ImageEntity image : images) {
            List<BoundingBox> boxes = boundingBoxRepository.findByImageId(image.getId());
            if (boxes.isEmpty()) {
                throw new IllegalArgumentException("Ảnh id=" + image.getId() + " chưa có bounding box.");
            }
            boxesByImageId.put(image.getId(), boxes);
            for (BoundingBox box : boxes) {
                classNamesSet.add(box.getLabelName());
            }
        }

        List<String> classNames = classNamesSet.stream().sorted().toList();
        Map<String, Integer> classIndexMap = new LinkedHashMap<>();
        for (int i = 0; i < classNames.size(); i++) {
            classIndexMap.put(classNames.get(i), i);
        }

        Path exportRoot = Paths.get(datasetRootDir, "training_exports");
        Files.createDirectories(exportRoot);

        String safeDatasetName = sanitizeFileName(dataset.getDatasetName());
        String folderName = safeDatasetName + "_" + dataset.getId() + "_" + LocalDateTime.now().format(FOLDER_TIME_FORMAT);
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

        List<ImageEntity> shuffledImages = new ArrayList<>(images);
        Collections.shuffle(shuffledImages, new Random(42L));

        int total = shuffledImages.size();
        int trainCount = (int) Math.floor(total * request.getTrainRatio());
        int valCount = (int) Math.floor(total * request.getValRatio());
        int testCount = total - trainCount - valCount;
        if (testCount < 0) {
            testCount = 0;
            valCount = Math.max(0, total - trainCount);
        }

        List<ImageEntity> trainImages = shuffledImages.subList(0, Math.min(trainCount, total));
        List<ImageEntity> valImages = shuffledImages.subList(Math.min(trainCount, total), Math.min(trainCount + valCount, total));
        List<ImageEntity> testImages = shuffledImages.subList(Math.min(trainCount + valCount, total), total);

        writeSplit(trainImages, imagesTrainDir, labelsTrainDir, boxesByImageId, classIndexMap);
        writeSplit(valImages, imagesValDir, labelsValDir, boxesByImageId, classIndexMap);
        writeSplit(testImages, imagesTestDir, labelsTestDir, boxesByImageId, classIndexMap);

        Path dataYaml = exportDir.resolve("data.yaml");
        writeDataYaml(dataYaml, exportDir, classNames);

        Path modelOutputDir = exportDir.resolve("runs");
        Files.createDirectories(modelOutputDir);

        return new SplitResult(exportDir, dataYaml, modelOutputDir);
    }

    private void writeSplit(
        List<ImageEntity> images,
        Path targetImagesDir,
        Path targetLabelsDir,
        Map<Long, List<BoundingBox>> boxesByImageId,
        Map<String, Integer> classIndexMap
    ) throws IOException {
        for (ImageEntity image : images) {
            Path source = Paths.get(image.getImagePath());
            if (!Files.exists(source)) {
                throw new IllegalArgumentException("File ảnh không tồn tại: " + image.getImagePath());
            }

            String fileName = buildOutputImageName(image);
            Path targetImage = targetImagesDir.resolve(fileName);
            Files.copy(source, targetImage, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            Path labelFile = targetLabelsDir.resolve(replaceExtension(fileName, ".txt"));
            List<String> lines = boxesByImageId.get(image.getId()).stream()
                .sorted(Comparator.comparing(BoundingBox::getId))
                .map(box -> toYoloLine(box, image, classIndexMap))
                .toList();
            Files.write(labelFile, lines, StandardCharsets.UTF_8);
        }
    }

    private String toYoloLine(BoundingBox box, ImageEntity image, Map<String, Integer> classIndexMap) {
        if (image.getImageWidth() == null || image.getImageHeight() == null || image.getImageWidth() <= 0 || image.getImageHeight() <= 0) {
            throw new IllegalArgumentException("Kích thước ảnh không hợp lệ cho image id=" + image.getId());
        }

        Integer classIndex = classIndexMap.get(box.getLabelName());
        if (classIndex == null) {
            throw new IllegalArgumentException("Không tìm thấy class cho label: " + box.getLabelName());
        }

        double xCenter = ((box.getXMin() + box.getXMax()) / 2.0) / image.getImageWidth();
        double yCenter = ((box.getYMin() + box.getYMax()) / 2.0) / image.getImageHeight();
        double width = (box.getXMax() - box.getXMin()) / image.getImageWidth();
        double height = (box.getYMax() - box.getYMin()) / image.getImageHeight();

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

    private String buildOutputImageName(ImageEntity image) {
        String baseName = replaceExtension(sanitizeFileName(image.getOriginalFilename()), getFileExtension(image.getImagePath()));
        if (baseName.isBlank()) {
            baseName = "image_" + image.getId() + getFileExtension(image.getImagePath());
        }
        return image.getId() + "_" + baseName;
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

    private record SplitResult(Path exportDir, Path dataYaml, Path modelOutputDir) {
    }
}
