package com.example.extract_service.service;

import com.example.extract_service.dto.AiExtractRequest;
import com.example.extract_service.dto.ExtractedCccdDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AiServiceClient {

    private final RestTemplate restTemplate;

    public ExtractedCccdDataDto extract(AiExtractRequest request) {
        ResponseEntity<ExtractedCccdDataDto> response = restTemplate.postForEntity(
            "http://AI-service/api/ai/extract",
            request,
            ExtractedCccdDataDto.class
        );
        return response.getBody();
    }
}
