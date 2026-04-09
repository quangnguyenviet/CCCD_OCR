package com.example.model_service.repository;

import com.example.model_service.entity.DatasetItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetItemRepository extends JpaRepository<DatasetItemEntity, Long> {
	List<DatasetItemEntity> findByDatasetId(Long datasetId);
}
