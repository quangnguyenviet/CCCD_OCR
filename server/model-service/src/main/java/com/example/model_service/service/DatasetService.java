package com.example.model_service.service;

import com.example.model_service.dto.dataset.DatasetOptionDto;
import com.example.model_service.dto.dataset.CreateDatasetRequestDto;
import com.example.model_service.dto.dataset.CreateDatasetResponseDto;
import com.example.model_service.entity.DatasetEntity;
import com.example.model_service.entity.DatasetItemEntity;
import com.example.model_service.entity.ImageEntity;
import com.example.model_service.repository.BoundingBoxRepository;
import com.example.model_service.repository.DatasetItemRepository;
import com.example.model_service.repository.DatasetRepository;
import com.example.model_service.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final DatasetItemRepository datasetItemRepository;
    private final ImageRepository imageRepository;
    private final BoundingBoxRepository boundingBoxRepository;

    @Transactional(readOnly = true)
    public List<DatasetOptionDto> listDatasets() {
        return datasetRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(dataset -> new DatasetOptionDto(
                dataset.getId(),
                dataset.getDatasetName(),
                dataset.getTotalImages(),
                dataset.getCreatedAt()
            ))
            .toList();
    }

    @Transactional
    public CreateDatasetResponseDto createDataset(CreateDatasetRequestDto request) {
        validateRequest(request);

        Set<Long> uniqueIds = new LinkedHashSet<>(request.getImageIds());
        List<ImageEntity> images = imageRepository.findAllById(uniqueIds);

        if (images.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("Có ảnh không tồn tại trong database.");
        }

        for (ImageEntity image : images) {
            long boxCount = boundingBoxRepository.countByImageId(image.getId());
            if (boxCount <= 0) {
                throw new IllegalArgumentException("Ảnh id=" + image.getId() + " chưa có bounding box, không thể tạo dataset.");
            }
        }

        DatasetEntity dataset = new DatasetEntity();
        dataset.setDatasetName(request.getDatasetName().trim());
        dataset.setTotalImages(images.size());
        dataset.setZipFileName(null);
        dataset.setZipFilePath(null);

        DatasetEntity savedDataset = datasetRepository.save(dataset);

        List<DatasetItemEntity> items = new ArrayList<>();
        for (ImageEntity image : images) {
            DatasetItemEntity item = new DatasetItemEntity();
            item.setDataset(savedDataset);
            item.setImage(image);
            items.add(item);
        }
        datasetItemRepository.saveAll(items);

        return new CreateDatasetResponseDto(
            savedDataset.getId(),
            savedDataset.getDatasetName(),
            savedDataset.getTotalImages(),
            savedDataset.getCreatedAt()
        );
    }

    private void validateRequest(CreateDatasetRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request không hợp lệ.");
        }

        if (request.getDatasetName() == null || request.getDatasetName().isBlank()) {
            throw new IllegalArgumentException("Tên dataset không được để trống.");
        }

        if (request.getImageIds() == null || request.getImageIds().isEmpty()) {
            throw new IllegalArgumentException("Cần chọn ít nhất một ảnh để tạo dataset.");
        }
    }
}
