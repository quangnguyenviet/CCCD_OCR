package com.example.model_service.service;

import com.example.model_service.entity.AiModel;
import com.example.model_service.entity.DatasetExport;
import com.example.model_service.repository.AiModelRepository;
import com.example.model_service.repository.DatasetExportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;
import java.util.Set;

@Service
public class TrainingService {

    private static final Set<AiModel.Status> TERMINAL_STATUSES =
            Set.of(AiModel.Status.SUCCESS, AiModel.Status.FAILED, AiModel.Status.STOPPED);

    @Autowired
    private AiModelRepository aiModelRepository;

    @Autowired
    private DatasetExportRepository datasetExportRepository;

    @Autowired
    private TrainingAsyncWorker asyncWorker;

    public Long createTrainingJobFromZip(MultipartFile file, String name, int epochs, int batchSize) {
        // 1. Lưu thông tin khởi tạo vào Database (Trạng thái PENDING)
        AiModel model = new AiModel();
        model.setName(name);
        model.setEpochs(epochs);
        model.setBatchSize(batchSize);
        model.setStatus(AiModel.Status.PENDING);
        model.setLatestLog("Đã đưa vào hàng đợi. Đang chuẩn bị xử lý file...");

        model = aiModelRepository.save(model);

        try {
            // LƯU Ý QUAN TRỌNG: Không truyền trực tiếp MultipartFile vào luồng Async
            // vì luồng HTTP sẽ đóng lại trước khi Async chạy xong, gây lỗi mất file.
            // Phải lưu file tạm ra đĩa hoặc đẩy lên Storage (S3/MinIO) ở luồng chính trước.

            // Giả lập lưu file ZIP tạm thời ra thư mục hệ thống
            String tempDirPath = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDirPath + "/" + model.getId() + "_" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // 2. Gọi tiến trình chạy ngầm (Background Job)
            asyncWorker.processZipAndTrain(model.getId(), tempFile.getAbsolutePath(), epochs, batchSize);

        } catch (Exception e) {
            model.setStatus(AiModel.Status.FAILED);
            model.setLatestLog("Lỗi khi lưu file: " + e.getMessage());
            aiModelRepository.save(model);
        }

        // 3. Trả về ID ngay lập tức, không chờ luồng Async chạy xong
        return model.getId();
    }

    public Long createTrainingJobFromDataset(Long datasetId, String name, int epochs, int batchSize) {
        DatasetExport datasetExport = datasetExportRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dataset_id: " + datasetId));

        AiModel model = new AiModel();
        model.setName(name);
        model.setEpochs(epochs);
        model.setBatchSize(batchSize);
        model.setDatasetId(datasetId);
        model.setStatus(AiModel.Status.PENDING);
        model.setLatestLog("Đã đưa vào hàng đợi. Đang chuẩn bị dataset từ CSDL...");

        model = aiModelRepository.save(model);

        try {
            String tempDirPath = System.getProperty("java.io.tmpdir");
            String datasetZipName = datasetExport.getZipFileName() != null && !datasetExport.getZipFileName().isBlank()
                    ? datasetExport.getZipFileName()
                    : ("dataset_" + datasetId + ".zip");

            File tempFile = new File(tempDirPath + "/" + model.getId() + "_" + datasetZipName);
            org.springframework.util.FileCopyUtils.copy(datasetExport.getZipData(), tempFile);

            asyncWorker.processZipAndTrain(model.getId(), tempFile.getAbsolutePath(), epochs, batchSize);
        } catch (Exception e) {
            model.setStatus(AiModel.Status.FAILED);
            model.setLatestLog("Lỗi khi chuẩn bị dataset từ CSDL: " + e.getMessage());
            aiModelRepository.save(model);
        }

        return model.getId();
    }

    public AiModel getTrainingStatus(Long modelId) {
        Optional<AiModel> modelInfo = aiModelRepository.findById(modelId);
        return modelInfo.orElseThrow(() -> new RuntimeException("Không tìm thấy Model ID: " + modelId));
    }

    public AiModel stopTrainingJob(Long modelId) {
        AiModel model = aiModelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Model ID: " + modelId));

        if (TERMINAL_STATUSES.contains(model.getStatus())) {
            return model;
        }

        boolean stopSignalSent = asyncWorker.requestStop(modelId);
        model.setStatus(AiModel.Status.STOPPED);
        model.setLatestLog(stopSignalSent
                ? "[STOPPED] Đã gửi tín hiệu dừng tiến trình huấn luyện."
                : "[STOPPED] Job đã được đánh dấu dừng.");

        return aiModelRepository.save(model);
    }
}