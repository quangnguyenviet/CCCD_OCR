package com.example.model_service.repository;

import com.example.model_service.entity.DatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends JpaRepository<DatasetEntity, Long> {
	List<DatasetEntity> findAllByOrderByCreatedAtDesc();
}
