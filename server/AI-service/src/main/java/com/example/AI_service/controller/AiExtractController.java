package com.example.AI_service.controller;

import com.example.AI_service.dto.AiExtractRequest;
import com.example.AI_service.dto.AiExtractResponse;
import com.example.AI_service.service.AiExtractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiExtractController {

    private final AiExtractService aiExtractService;

    @PostMapping("/extract")
    public ResponseEntity<AiExtractResponse> extract(@RequestBody AiExtractRequest request) {
        log.info(
            "Received extract request: cardModelUrl={}, roiModelUrl={}, ocrModelUrl={}",
            request.getCardModelUrl(),
            request.getRoiModelUrl(),
            request.getOcrModelUrl()
        );

        AiExtractResponse response = aiExtractService.extract(request);
        return ResponseEntity.ok(response);
    }
}
