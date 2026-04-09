package com.example.model_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "dataset_export_items")
@Data
public class DatasetExportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_export_id", nullable = false)
    private DatasetExport datasetExport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labeled_image_id", nullable = false)
    private LabeledImage labeledImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
