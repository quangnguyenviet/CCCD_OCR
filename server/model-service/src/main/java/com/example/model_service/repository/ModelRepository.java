package com.example.model_service.repository;

import com.example.model_service.entity.Model;
import com.example.model_service.enums.ModelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    List<Model> findByTypeAndStatus(ModelType type, String status);
}
