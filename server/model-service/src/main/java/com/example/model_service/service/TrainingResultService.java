//package com.example.model_service.service;
//
//import com.example.model_service.dto.training.TrainingResultImageDto;
//import com.example.model_service.dto.training.TrainingResultListResponseDto;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.util.UriUtils;
//
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Stream;
//
//@Service
//public class TrainingResultService {
//
//    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".bmp", ".webp");
//
//    @Value("${upload.dataset.dir}")
//    private String datasetRootDir;
//
//    public TrainingResultListResponseDto listResultImages(String modelOutputDir, String name) throws Exception {
//        if (name == null || name.isBlank()) {
//            throw new IllegalArgumentException("Thiếu tên run huấn luyện.");
//        }
//
//        Path outputDir = resolveSafePath(modelOutputDir);
//        Path runDir = outputDir.resolve(name).normalize();
//
//        if (!runDir.startsWith(outputDir)) {
//            throw new IllegalArgumentException("Đường dẫn kết quả huấn luyện không hợp lệ.");
//        }
//
//        if (!Files.exists(runDir) || !Files.isDirectory(runDir)) {
//            throw new IllegalArgumentException("Chưa tìm thấy thư mục kết quả huấn luyện: " + runDir);
//        }
//
//        List<TrainingResultImageDto> images;
//        try (Stream<Path> stream = Files.walk(runDir, 3)) {
//            images = stream
//                .filter(Files::isRegularFile)
//                .filter(this::isImageFile)
//                .sorted(Comparator.comparing(Path::toString))
//                .map(path -> {
//                    String absPath = path.toAbsolutePath().normalize().toString();
//                    String encoded = UriUtils.encode(absPath, StandardCharsets.UTF_8);
//                    return new TrainingResultImageDto(
//                        path.getFileName().toString(),
//                        absPath,
//                        "/api/training/results/file?imagePath=" + encoded
//                    );
//                })
//                .toList();
//        }
//
//        return new TrainingResultListResponseDto(runDir.toString(), images);
//    }
//
//    public Path resolveResultImagePath(String imagePath) {
//        if (imagePath == null || imagePath.isBlank()) {
//            throw new IllegalArgumentException("imagePath không hợp lệ.");
//        }
//
//        Path path = Paths.get(imagePath).toAbsolutePath().normalize();
//        Path baseDir = Paths.get(datasetRootDir, "training_exports").toAbsolutePath().normalize();
//
//        if (!path.startsWith(baseDir)) {
//            throw new IllegalArgumentException("Đường dẫn ảnh kết quả không hợp lệ.");
//        }
//
//        if (!Files.exists(path) || !Files.isRegularFile(path)) {
//            throw new IllegalArgumentException("Không tìm thấy file ảnh kết quả.");
//        }
//
//        if (!isImageFile(path)) {
//            throw new IllegalArgumentException("File không phải ảnh hợp lệ.");
//        }
//
//        return path;
//    }
//
//    private Path resolveSafePath(String modelOutputDir) {
//        if (modelOutputDir == null || modelOutputDir.isBlank()) {
//            throw new IllegalArgumentException("modelOutputDir không hợp lệ.");
//        }
//
//        Path baseDir = Paths.get(datasetRootDir, "training_exports").toAbsolutePath().normalize();
//        Path target = Paths.get(modelOutputDir).toAbsolutePath().normalize();
//
//        if (!target.startsWith(baseDir)) {
//            throw new IllegalArgumentException("Đường dẫn model output không hợp lệ.");
//        }
//
//        return target;
//    }
//
//    private boolean isImageFile(Path path) {
//        String name = path.getFileName().toString().toLowerCase();
//        return IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
//    }
//}
