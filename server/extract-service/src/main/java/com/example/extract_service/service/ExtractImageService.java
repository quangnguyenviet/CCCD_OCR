package com.example.extract_service.service;

import com.example.extract_service.dto.AiExtractRequest;
import com.example.extract_service.dto.ExtractImageResponseDto;
import com.example.extract_service.dto.ExtractedCccdDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ExtractImageService {

    private final AiServiceClient aiServiceClient;

    public ExtractImageResponseDto extract(
        MultipartFile imageFile,
        String cardModelUrl,
        String roiModelUrl,
        String ocrModelUrl
    ) {
        long start = System.currentTimeMillis();

        AiExtractRequest request = new AiExtractRequest(
            cardModelUrl,
            roiModelUrl,
            ocrModelUrl,
            buildImageReference(imageFile)
        );

        ExtractedCccdDataDto data = aiServiceClient.extract(request);

        long end = System.currentTimeMillis();
        return new ExtractImageResponseDto(
            "success",
            "Trích xuất thành công",
            end - start,
            data
        );
    }

    private String buildImageReference(MultipartFile imageFile) {
        if (imageFile == null || imageFile.getOriginalFilename() == null) {
            return "multipart://unknown";
        }
        return "multipart://" + imageFile.getOriginalFilename();
    }
}
