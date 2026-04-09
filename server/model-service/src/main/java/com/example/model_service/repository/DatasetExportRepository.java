package com.example.model_service.repository;

import com.example.model_service.entity.DatasetExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetExportRepository extends JpaRepository<DatasetExport, Long> {

    @Query("SELECT de FROM DatasetExport de ORDER BY de.createdAt DESC")
    List<DatasetExport> findAllOrderByCreatedAtDesc();
}
