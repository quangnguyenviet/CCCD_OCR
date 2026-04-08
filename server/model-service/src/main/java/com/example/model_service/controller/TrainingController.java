package com.example.model_service.controller;

import com.example.model_service.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/training/roi-detection")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    // API 2.1: Khởi tạo huấn luyện bằng file ZIP
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> trainWithZip(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("epochs") Integer epochs,
            @RequestParam("batch_size") Integer batchSize) {

        // Gọi Service xử lý và lấy về modelId ngay lập tức
        Long modelId = trainingService.createTrainingJobFromZip(file, name, epochs, batchSize);

        // Trả về JSON theo đúng đặc tả
        return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "message", "Đã tiếp nhận file zip và đưa vào hàng đợi huấn luyện",
                "data", Map.of("model_id", modelId)
        ));
    }

    // API 2.2: Frontend gọi liên tục (Polling) để lấy trạng thái
    @GetMapping("/status/{modelId}")
    public ResponseEntity<?> getTrainingStatus(@PathVariable Long modelId) {
        return ResponseEntity.ok(trainingService.getTrainingStatus(modelId));
    }

    // API 2.3: Dừng huấn luyện thủ công
    @PostMapping("/stop/{modelId}")
    public ResponseEntity<?> stopTraining(@PathVariable Long modelId) {
        return ResponseEntity.ok(trainingService.stopTrainingJob(modelId));
    }
}
