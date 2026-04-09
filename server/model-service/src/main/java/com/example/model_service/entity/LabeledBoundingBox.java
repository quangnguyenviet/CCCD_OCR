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
@Table(name = "labeled_bounding_boxes")
@Data
public class LabeledBoundingBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labeled_image_id", nullable = false)
    private LabeledImage labeledImage;

    @Column(name = "label_name", nullable = false)
    private String labelName;

    @Column(name = "x_min", nullable = false)
    private Double xMin;

    @Column(name = "y_min", nullable = false)
    private Double yMin;

    @Column(name = "x_max", nullable = false)
    private Double xMax;

    @Column(name = "y_max", nullable = false)
    private Double yMax;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
