package com.example.model_service.service;

import com.example.model_service.dto.labeling.BoundingBoxRequestDto;
import com.example.model_service.dto.labeling.BoundingBoxResponseDto;
import com.example.model_service.dto.labeling.ImageUploadResponseDto;
import com.example.model_service.entity.BoundingBox;
import com.example.model_service.entity.ImageEntity;
import com.example.model_service.repository.BoundingBoxRepository;
import com.example.model_service.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataLabelingService {

    private final ImageRepository imageRepository;
    private final BoundingBoxRepository boundingBoxRepository;

    @Value("${upload.image.dir}")
    private String uploadImageDir;

    @Transactional
    public ImageUploadResponseDto uploadImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Ảnh tải lên không hợp lệ.");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Tệp tải lên phải là ảnh.");
        }

        Path uploadDir = Paths.get(uploadImageDir);
        Files.createDirectories(uploadDir);

        String originalFilename = imageFile.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + extension;
        Path destination = uploadDir.resolve(storedFilename);

        Files.copy(imageFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        BufferedImage bufferedImage = ImageIO.read(destination.toFile());
        if (bufferedImage == null) {
            Files.deleteIfExists(destination);
            throw new IllegalArgumentException("Không đọc được nội dung ảnh.");
        }

        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setOriginalFilename(originalFilename == null ? storedFilename : originalFilename);
        imageEntity.setMimeType(contentType);
        imageEntity.setImageWidth(bufferedImage.getWidth());
        imageEntity.setImageHeight(bufferedImage.getHeight());
        imageEntity.setImagePath(destination.toString());

        ImageEntity saved = imageRepository.save(imageEntity);

        return new ImageUploadResponseDto(
            saved.getId(),
            saved.getOriginalFilename(),
            saved.getImageWidth(),
            saved.getImageHeight(),
            saved.getImagePath()
        );
    }

    @Transactional
    public List<BoundingBoxResponseDto> saveBoundingBoxes(Long imageId, List<BoundingBoxRequestDto> boxes) {
        ImageEntity imageEntity = imageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ảnh với id=" + imageId));

        List<BoundingBoxRequestDto> safeBoxes = boxes == null ? Collections.emptyList() : boxes;

        for (BoundingBoxRequestDto box : safeBoxes) {
            validateBox(box);
        }

        boundingBoxRepository.deleteByImageId(imageId);

        List<BoundingBox> entities = safeBoxes.stream().map(box -> {
            BoundingBox entity = new BoundingBox();
            entity.setImage(imageEntity);
            entity.setLabelName(box.getLabelName().trim());
            entity.setXMin(box.getXMin());
            entity.setYMin(box.getYMin());
            entity.setXMax(box.getXMax());
            entity.setYMax(box.getYMax());
            return entity;
        }).toList();

        return boundingBoxRepository.saveAll(entities).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<BoundingBoxResponseDto> getBoundingBoxes(Long imageId) {
        return boundingBoxRepository.findByImageId(imageId).stream()
            .map(this::toResponse)
            .toList();
    }

    private BoundingBoxResponseDto toResponse(BoundingBox entity) {
        return new BoundingBoxResponseDto(
            entity.getId(),
            entity.getImage().getId(),
            entity.getLabelName(),
            entity.getXMin(),
            entity.getYMin(),
            entity.getXMax(),
            entity.getYMax()
        );
    }

    private void validateBox(BoundingBoxRequestDto box) {
        if (box == null) {
            throw new IllegalArgumentException("Bounding box không hợp lệ.");
        }

        if (box.getLabelName() == null || box.getLabelName().isBlank()) {
            throw new IllegalArgumentException("Nhãn không được để trống.");
        }

        if (box.getXMin() == null || box.getYMin() == null || box.getXMax() == null || box.getYMax() == null) {
            throw new IllegalArgumentException("Thiếu tọa độ bounding box.");
        }

        if (box.getXMin() >= box.getXMax() || box.getYMin() >= box.getYMax()) {
            throw new IllegalArgumentException("Bounding box không hợp lệ: cần x_min < x_max và y_min < y_max.");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex);
    }
}
