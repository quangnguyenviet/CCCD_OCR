//package com.example.model_service.controller;
//
//import com.example.model_service.dto.training.TrainingLogResponseDto;
//import com.example.model_service.dto.training.TrainingResultListResponseDto;
//import com.example.model_service.service.TrainingLogService;
//import com.example.model_service.service.TrainingResultService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/training")
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin(origins = "*")
//public class TrainingLogController {
//
//    private final TrainingLogService trainingLogService;
//    private final TrainingResultService trainingResultService;
//
//    @GetMapping("/logs")
//    public ResponseEntity<?> getLogs(
//        @RequestParam("logFilePath") String logFilePath,
//        @RequestParam(value = "tailLines", required = false) Integer tailLines
//    ) {
//        try {
//            TrainingLogResponseDto response = trainingLogService.readLog(logFilePath, tailLines);
//            return ResponseEntity.ok(response);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        } catch (IOException e) {
//            log.error("Read training logs failed", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("message", "Không thể đọc log huấn luyện."));
//        }
//    }
//
//    @GetMapping("/results")
//    public ResponseEntity<?> getTrainingResultImages(
//        @RequestParam("modelOutputDir") String modelOutputDir,
//        @RequestParam("name") String name
//    ) {
//        try {
//            TrainingResultListResponseDto response = trainingResultService.listResultImages(modelOutputDir, name);
//            return ResponseEntity.ok(response);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        } catch (Exception e) {
//            log.error("Read training result images failed", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("message", "Không thể lấy ảnh kết quả huấn luyện."));
//        }
//    }
//
//    @GetMapping("/results/file")
//    public ResponseEntity<?> getTrainingResultImage(
//        @RequestParam("imagePath") String imagePath
//    ) {
//        try {
//            java.nio.file.Path path = trainingResultService.resolveResultImagePath(imagePath);
//            Resource resource = new FileSystemResource(path);
//            return ResponseEntity.ok()
//                .contentType(resolveMediaType(path))
//                .body(resource);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        } catch (Exception e) {
//            log.error("Read training result image failed", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("message", "Không thể đọc ảnh kết quả huấn luyện."));
//        }
//    }
//
//    private MediaType resolveMediaType(java.nio.file.Path path) {
//        String fileName = path.getFileName().toString().toLowerCase();
//        if (fileName.endsWith(".png")) {
//            return MediaType.IMAGE_PNG;
//        }
//        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
//            return MediaType.IMAGE_JPEG;
//        }
//        if (fileName.endsWith(".webp")) {
//            return MediaType.parseMediaType("image/webp");
//        }
//        if (fileName.endsWith(".bmp")) {
//            return MediaType.parseMediaType("image/bmp");
//        }
//        return MediaType.APPLICATION_OCTET_STREAM;
//    }
//}
