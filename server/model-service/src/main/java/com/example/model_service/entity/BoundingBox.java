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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bounding_boxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoundingBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private ImageEntity image;

    @Column(name = "label_name", nullable = false, length = 100)
    private String labelName;

    @Column(name = "x_min", nullable = false)
    private Double xMin;

    @Column(name = "y_min", nullable = false)
    private Double yMin;

    @Column(name = "x_max", nullable = false)
    private Double xMax;

    @Column(name = "y_max", nullable = false)
    private Double yMax;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
