package com.example.model_service.repository;

import com.example.model_service.entity.BoundingBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoundingBoxRepository extends JpaRepository<BoundingBox, Long> {
    List<BoundingBox> findByImageId(Long imageId);

    void deleteByImageId(Long imageId);
}
