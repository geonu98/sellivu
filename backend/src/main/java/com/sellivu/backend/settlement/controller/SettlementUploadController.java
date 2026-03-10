package com.sellivu.backend.settlement.controller;

import com.sellivu.backend.settlement.dto.SettlementUploadResponse;
import com.sellivu.backend.settlement.service.SettlementUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/settlement/uploads")
public class SettlementUploadController {

    private final SettlementUploadService settlementUploadService;

    public SettlementUploadController(SettlementUploadService settlementUploadService) {
        this.settlementUploadService = settlementUploadService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SettlementUploadResponse> upload(
            @RequestPart("file") MultipartFile file
    ) {
        SettlementUploadResponse response = settlementUploadService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}