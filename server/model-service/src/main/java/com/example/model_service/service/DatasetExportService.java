package com.example.model_service.service;

import com.example.model_service.dto.labeling.CreateDatasetRequestDto;
import com.example.model_service.dto.labeling.DatasetSummaryDto;
import com.example.model_service.entity.DatasetExport;
import com.example.model_service.entity.DatasetExportItem;
import com.example.model_service.entity.LabeledBoundingBox;
import com.example.model_service.entity.LabeledImage;
import com.example.model_service.repository.DatasetExportItemRepository;
import com.example.model_service.repository.DatasetExportRepository;
import com.example.model_service.repository.LabeledBoundingBoxRepository;
import com.example.model_service.repository.LabeledImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DatasetExportService {

    private final LabeledImageRepository labeledImageRepository;
    private final LabeledBoundingBoxRepository labeledBoundingBoxRepository;
    private final DatasetExportRepository datasetExportRepository;
    private final DatasetExportItemRepository datasetExportItemRepository;

    public DatasetExportService(
            LabeledImageRepository labeledImageRepository,
            LabeledBoundingBoxRepository labeledBoundingBoxRepository,
            DatasetExportRepository datasetExportRepository,
            DatasetExportItemRepository datasetExportItemRepository
    ) {
        this.labeledImageRepository = labeledImageRepository;
        this.labeledBoundingBoxRepository = labeledBoundingBoxRepository;
        this.datasetExportRepository = datasetExportRepository;
        this.datasetExportItemRepository = datasetExportItemRepository;
    }

    @Transactional
    public DatasetSummaryDto createDataset(CreateDatasetRequestDto request) throws IOException {
        List<Long> imageIds = request != null ? request.getImageIds() : Collections.emptyList();
        String datasetName = request != null ? request.getDatasetName() : null;

        if (datasetName == null || datasetName.isBlank()) {
            throw new RuntimeException("dataset_name không được để trống");
        }

        if (imageIds == null || imageIds.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất 1 mẫu đã gán nhãn");
        }

        List<LabeledImage> selectedImages = fetchSelectedImages(imageIds);
        byte[] zipData = buildZipForImages(selectedImages);
        String zipFileName = buildZipFileName(datasetName);

        DatasetExport datasetExport = new DatasetExport();
        datasetExport.setDatasetName(datasetName.trim());
        datasetExport.setZipFileName(zipFileName);
        datasetExport.setZipData(zipData);
        datasetExport.setTotalImages(selectedImages.size());

        DatasetExport savedDataset = datasetExportRepository.save(datasetExport);

        for (LabeledImage image : selectedImages) {
            DatasetExportItem item = new DatasetExportItem();
            item.setDatasetExport(savedDataset);
            item.setLabeledImage(image);
            datasetExportItemRepository.save(item);
        }

        return toDatasetSummary(savedDataset);
    }

    public byte[] exportDatasetById(Long datasetId) {
        DatasetExport dataset = datasetExportRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dataset_id: " + datasetId));
        return dataset.getZipData();
    }

    public String getDatasetFileNameById(Long datasetId) {
        DatasetExport dataset = datasetExportRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dataset_id: " + datasetId));
        return dataset.getZipFileName();
    }

    public List<DatasetSummaryDto> getDatasets() {
        return datasetExportRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::toDatasetSummary)
                .toList();
    }

    private DatasetSummaryDto toDatasetSummary(DatasetExport datasetExport) {
        return new DatasetSummaryDto(
                datasetExport.getId(),
                datasetExport.getDatasetName(),
                datasetExport.getZipFileName(),
                datasetExport.getTotalImages(),
                datasetExport.getCreatedAt()
        );
    }

    private List<LabeledImage> fetchSelectedImages(List<Long> imageIds) {
        LinkedHashSet<Long> orderedIds = new LinkedHashSet<>(imageIds);
        List<LabeledImage> loadedImages = labeledImageRepository.findAllById(orderedIds);
        Map<Long, LabeledImage> imageMap = new HashMap<>();
        for (LabeledImage image : loadedImages) {
            imageMap.put(image.getId(), image);
        }

        List<LabeledImage> selectedImages = new ArrayList<>();
        for (Long id : orderedIds) {
            LabeledImage image = imageMap.get(id);
            if (image == null) {
                throw new RuntimeException("Không tìm thấy image_id: " + id);
            }

            List<LabeledBoundingBox> boxes = labeledBoundingBoxRepository.findByLabeledImageId(id);
            if (boxes.isEmpty()) {
                throw new RuntimeException("Ảnh image_id=" + id + " chưa có nhãn");
            }

            selectedImages.add(image);
        }

        return selectedImages;
    }

    private byte[] buildZipForImages(List<LabeledImage> images) throws IOException {
        if (images.isEmpty()) {
            throw new RuntimeException("Không có ảnh nào để export");
        }

        Set<String> classNames = collectClassNames(images);
        List<LabeledImage> shuffledImages = new ArrayList<>(images);
        Collections.shuffle(shuffledImages, new Random(42L));

        int total = shuffledImages.size();
        int trainCount = Math.max(1, (int) Math.round(total * 0.8));
        int valCount = total >= 2 ? Math.max(1, (int) Math.round(total * 0.1)) : 0;
        if (trainCount + valCount > total) {
            valCount = Math.max(0, total - trainCount);
        }

        int testCount = total - trainCount - valCount;
        if (total >= 3 && testCount == 0 && trainCount > 1) {
            trainCount -= 1;
            testCount = 1;
        }

        String valPath = valCount > 0 ? "images/val" : "images/train";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Export images and labels by split (train/val/test)
            int imageIndex = 1;
            for (LabeledImage img : shuffledImages) {
                String split;
                if (imageIndex <= trainCount) {
                    split = "train";
                } else if (imageIndex <= trainCount + valCount) {
                    split = "val";
                } else {
                    split = "test";
                }

                String imageName = String.format("image_%05d.jpg", imageIndex);
                String labelName = String.format("image_%05d.txt", imageIndex);

                // Add image to zip
                zos.putNextEntry(new ZipEntry("images/" + split + "/" + imageName));
                zos.write(img.getImageData());
                zos.closeEntry();

                // Add label to zip
                List<LabeledBoundingBox> boxes = labeledBoundingBoxRepository.findByLabeledImageId(img.getId());
                String labelContent = generateYoloLabel(boxes, img, classNames);
                zos.putNextEntry(new ZipEntry("labels/" + split + "/" + labelName));
                zos.write(labelContent.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();

                imageIndex++;
            }

            // Add data.yaml
            String yamlContent = generateDataYaml(classNames, valPath, testCount > 0);
            zos.putNextEntry(new ZipEntry("data.yaml"));
            zos.write(yamlContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        return baos.toByteArray();
    }

    private Set<String> collectClassNames(List<LabeledImage> images) {
        Set<String> classNames = new TreeSet<>();
        for (LabeledImage img : images) {
            List<LabeledBoundingBox> boxes = labeledBoundingBoxRepository.findByLabeledImageId(img.getId());
            for (LabeledBoundingBox box : boxes) {
                classNames.add(box.getLabelName());
            }
        }
        return classNames;
    }

    private String generateYoloLabel(List<LabeledBoundingBox> boxes, LabeledImage image, Set<String> classNames) {
        List<String> classList = new ArrayList<>(classNames);
        StringBuilder sb = new StringBuilder();

        for (LabeledBoundingBox box : boxes) {
            int classIndex = classList.indexOf(box.getLabelName());
            double centerX = (box.getXMin() + box.getXMax()) / 2.0 / image.getImageWidth();
            double centerY = (box.getYMin() + box.getYMax()) / 2.0 / image.getImageHeight();
            double width = (box.getXMax() - box.getXMin()) / (double) image.getImageWidth();
            double height = (box.getYMax() - box.getYMin()) / (double) image.getImageHeight();

            sb.append(classIndex).append(" ")
                    .append(String.format(Locale.US, "%.6f", centerX)).append(" ")
                    .append(String.format(Locale.US, "%.6f", centerY)).append(" ")
                    .append(String.format(Locale.US, "%.6f", width)).append(" ")
                    .append(String.format(Locale.US, "%.6f", height)).append("\n");
        }

        return sb.toString();
    }

    private String generateDataYaml(Set<String> classNames, String valPath, boolean hasTestSplit) {
        List<String> classList = new ArrayList<>(classNames);
        StringBuilder sb = new StringBuilder();

        sb.append("path: .\n");
        sb.append("train: images/train\n");
        sb.append("val: ").append(valPath).append("\n");
        if (hasTestSplit) {
            sb.append("test: images/test\n");
        }
        sb.append("nc: ").append(classList.size()).append("\n");
        sb.append("names: [");

        for (int i = 0; i < classList.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("'").append(classList.get(i)).append("'");
        }
        sb.append("]\n");

        return sb.toString();
    }

    private String buildZipFileName(String datasetName) {
        String slug = datasetName.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (slug.isBlank()) {
            slug = "dataset";
        }

        return slug + "_" + LocalDateTime.now().toString().replace(":", "-") + ".zip";
    }
}
