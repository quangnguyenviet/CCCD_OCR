package com.example.model_service.repository;

import com.example.model_service.entity.DatasetExportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetExportItemRepository extends JpaRepository<DatasetExportItem, Long> {
}
