package com.example.model_service.service;

import com.example.model_service.dto.dataset.ImageOptionDto;
import com.example.model_service.entity.ImageEntity;
import com.example.model_service.repository.BoundingBoxRepository;
import com.example.model_service.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final BoundingBoxRepository boundingBoxRepository;

    @Transactional(readOnly = true)
    public List<ImageOptionDto> listImages() {
        return imageRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(image -> new ImageOptionDto(
                image.getId(),
                image.getOriginalFilename(),
                image.getImageWidth(),
                image.getImageHeight(),
                "/api/images/" + image.getId() + "/preview",
                boundingBoxRepository.countByImageId(image.getId()),
                image.getCreatedAt()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public ImageEntity getImageOrThrow(Long imageId) {
        return imageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ảnh với id=" + imageId));
    }

    @Transactional(readOnly = true)
    public Resource loadImageResource(Long imageId) {
        ImageEntity image = getImageOrThrow(imageId);
        if (image.getImagePath() == null || image.getImagePath().isBlank()) {
            throw new IllegalArgumentException("Ảnh chưa có đường dẫn vật lý.");
        }

        Path path = Paths.get(image.getImagePath());
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File ảnh không tồn tại trên hệ thống.");
        }

        return new FileSystemResource(path);
    }
}
