package com.example.AI_service.service;

import com.example.AI_service.dto.AiExtractRequest;
import com.example.AI_service.dto.AiExtractResponse;
import org.springframework.stereotype.Service;

@Service
public class AiExtractService {

    public AiExtractResponse extract(AiExtractRequest request) {
        return new AiExtractResponse(
            "001099000123",
            "NGUYEN VAN A",
            "01/01/1999",
            "Nam",
            "Việt Nam",
            "Ba Đình, Hà Nội",
            "Hoàn Kiếm, Hà Nội"
        );
    }
}
