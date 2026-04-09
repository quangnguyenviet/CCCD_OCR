package com.example.model_service.repository;

import com.example.model_service.entity.LabeledBoundingBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabeledBoundingBoxRepository extends JpaRepository<LabeledBoundingBox, Long> {
    void deleteByLabeledImageId(Long labeledImageId);
    List<LabeledBoundingBox> findByLabeledImageId(Long labeledImageId);
}
