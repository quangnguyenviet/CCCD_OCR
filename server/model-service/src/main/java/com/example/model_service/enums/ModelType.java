package com.example.model_service.enums;

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
