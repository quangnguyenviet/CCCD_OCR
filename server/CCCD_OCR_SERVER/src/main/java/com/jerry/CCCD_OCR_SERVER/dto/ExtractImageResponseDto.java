package com.jerry.CCCD_OCR_SERVER.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractImageResponseDto {

    private String status;

    private String message;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    private ExtractedCccdDataDto data;
}
