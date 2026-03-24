package com.jerry.CCCD_OCR_SERVER.enums;

/**
 * Enum định nghĩa các loại mô hình AI trong hệ thống OCR CCCD
 */
public enum ModelType {
    CARD_DETECTION("Nhận diện thẻ CCCD"),
    ROI_DETECTION("Nhận diện vùng quan trọng"),
    OCR("Nhận dạng ký tự quang học");

    private final String description;

    ModelType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
