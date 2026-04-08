package com.example.model_service.repository;

import com.example.model_service.entity.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiModelRepository extends JpaRepository<AiModel, Long> {
    List<AiModel> findByTypeAndStatus(String type, AiModel.Status status);
}
