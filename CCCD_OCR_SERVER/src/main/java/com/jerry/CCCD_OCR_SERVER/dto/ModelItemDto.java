package com.jerry.CCCD_OCR_SERVER.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO đại diện cho một mô hình trong response danh sách mô hình
 */
@Getter
@Setter
@AllArgsConstructor
public class ModelItemDto {
    private String id;
    private String name;
}
