package com.example.model_service.service;

import com.example.model_service.dto.labeling.AnnotatedImageItemDto;
import com.example.model_service.dto.labeling.BoundingBoxRequestDto;
import com.example.model_service.dto.labeling.SaveAnnotationsRequestDto;
import com.example.model_service.dto.labeling.SaveAnnotationsResponseDto;
import com.example.model_service.dto.labeling.UploadImageResponseDto;
import com.example.model_service.entity.LabeledBoundingBox;
import com.example.model_service.entity.LabeledImage;
import com.example.model_service.repository.LabeledBoundingBoxRepository;
import com.example.model_service.repository.LabeledImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabelingService {

    private final LabeledImageRepository labeledImageRepository;
    private final LabeledBoundingBoxRepository labeledBoundingBoxRepository;

    public LabelingService(
            LabeledImageRepository labeledImageRepository,
            LabeledBoundingBoxRepository labeledBoundingBoxRepository
    ) {
        this.labeledImageRepository = labeledImageRepository;
        this.labeledBoundingBoxRepository = labeledBoundingBoxRepository;
    }

    public UploadImageResponseDto uploadImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new RuntimeException("File ảnh không hợp lệ hoặc rỗng");
        }

        try {
            byte[] bytes = imageFile.getBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new RuntimeException("Không thể đọc ảnh. Vui lòng upload đúng định dạng ảnh.");
            }

            LabeledImage labeledImage = new LabeledImage();
            labeledImage.setOriginalFilename(imageFile.getOriginalFilename());
            labeledImage.setMimeType(imageFile.getContentType());
            labeledImage.setImageWidth(image.getWidth());
            labeledImage.setImageHeight(image.getHeight());
            labeledImage.setImageData(bytes);

            LabeledImage saved = labeledImageRepository.save(labeledImage);
            return new UploadImageResponseDto(
                    saved.getId(),
                    saved.getOriginalFilename(),
                    saved.getImageWidth(),
                    saved.getImageHeight()
            );
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc dữ liệu ảnh: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SaveAnnotationsResponseDto saveAnnotations(Long imageId, SaveAnnotationsRequestDto request) {
        LabeledImage labeledImage = labeledImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy image_id: " + imageId));

        List<BoundingBoxRequestDto> annotations =
                request != null && request.getAnnotations() != null ? request.getAnnotations() : Collections.emptyList();

        labeledBoundingBoxRepository.deleteByLabeledImageId(imageId);

        for (BoundingBoxRequestDto annotation : annotations) {
            validateBoundingBox(annotation, labeledImage);

            LabeledBoundingBox box = new LabeledBoundingBox();
            box.setLabeledImage(labeledImage);
            box.setLabelName(annotation.getLabelName());
            box.setXMin(annotation.getXMin());
            box.setYMin(annotation.getYMin());
            box.setXMax(annotation.getXMax());
            box.setYMax(annotation.getYMax());
            labeledBoundingBoxRepository.save(box);
        }

        return new SaveAnnotationsResponseDto(imageId, annotations.size());
    }

    private void validateBoundingBox(BoundingBoxRequestDto box, LabeledImage image) {
        if (box == null || box.getLabelName() == null || box.getLabelName().isBlank()) {
            throw new RuntimeException("Thiếu label_name cho bounding box");
        }

        if (box.getXMin() == null || box.getYMin() == null || box.getXMax() == null || box.getYMax() == null) {
            throw new RuntimeException("Bounding box phải có đủ x_min, y_min, x_max, y_max");
        }

        if (box.getXMin() >= box.getXMax() || box.getYMin() >= box.getYMax()) {
            throw new RuntimeException("Bounding box không hợp lệ: x_min/x_max hoặc y_min/y_max");
        }

        if (box.getXMin() < 0 || box.getYMin() < 0 || box.getXMax() > image.getImageWidth() || box.getYMax() > image.getImageHeight()) {
            throw new RuntimeException("Bounding box vượt quá kích thước ảnh gốc");
        }
    }

    public List<AnnotatedImageItemDto> getAnnotatedImages() {
        return labeledImageRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(image -> {
                    int annotationCount = labeledBoundingBoxRepository.findByLabeledImageId(image.getId()).size();
                    return new AnnotatedImageItemDto(
                            image.getId(),
                            image.getOriginalFilename(),
                            image.getImageWidth(),
                            image.getImageHeight(),
                            annotationCount,
                            image.getCreatedAt()
                    );
                })
                .filter(item -> item.getAnnotationCount() > 0)
                .collect(Collectors.toList());
    }

    public LabeledImage getLabeledImageById(Long imageId) {
        return labeledImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy image_id: " + imageId));
    }
}
