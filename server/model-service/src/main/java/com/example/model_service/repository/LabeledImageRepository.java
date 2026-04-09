package com.example.model_service.repository;

import com.example.model_service.entity.LabeledImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabeledImageRepository extends JpaRepository<LabeledImage, Long> {
    @Query("SELECT li FROM LabeledImage li ORDER BY li.createdAt DESC")
    List<LabeledImage> findAllOrderByCreatedAtDesc();
}
