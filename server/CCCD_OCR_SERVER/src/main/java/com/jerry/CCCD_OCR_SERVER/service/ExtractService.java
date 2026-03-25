package com.jerry.CCCD_OCR_SERVER.service;

import com.jerry.CCCD_OCR_SERVER.dto.ExtractImageResponseDto;
import com.jerry.CCCD_OCR_SERVER.dto.ExtractedCccdDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ExtractService {

    public ExtractImageResponseDto extractFromImage(
        MultipartFile imageFile,
        String cardModelId,
        String roiModelId,
        String ocrModelId
    ) {
        log.info(
            "Mock extract from image. fileName={}, cardModelId={}, roiModelId={}, ocrModelId={}",
            imageFile != null ? imageFile.getOriginalFilename() : null,
            cardModelId,
            roiModelId,
            ocrModelId
        );

        return ExtractImageResponseDto.builder()
            .status("success")
            .message("Trích xuất thành công")
            .processingTimeMs(2450L)
            .data(new ExtractedCccdDataDto(
                "001099000123",
                "NGUYEN VAN A",
                "01/01/1999",
                "Nam",
                "Việt Nam",
                "Ba Đình, Hà Nội",
                "Hoàn Kiếm, Hà Nội"
            ))
            .build();
    }
}
