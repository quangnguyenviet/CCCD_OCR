package com.jerry.CCCD_OCR_SERVER.repository;

import com.jerry.CCCD_OCR_SERVER.entity.Model;
import com.jerry.CCCD_OCR_SERVER.enums.ModelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository cho entity Model
 */
@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    
    /**
     * Tìm các mô hình hoạt động theo loại
     * @param type loại mô hình
     * @param isActive trạng thái hoạt động
     * @return danh sách mô hình
     */
    List<Model> findByTypeAndIsActive(ModelType type, Boolean isActive);
    
    /**
     * Tìm tất cả mô hình hoạt động
     * @return danh sách mô hình hoạt động
     */
    @Query("SELECT m FROM Model m WHERE m.isActive = true ORDER BY m.type, m.name")
    List<Model> findAllActiveModels();
}
