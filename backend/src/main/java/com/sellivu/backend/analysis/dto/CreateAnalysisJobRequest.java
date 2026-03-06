package com.sellivu.backend.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateAnalysisJobRequest {

    @NotBlank(message = "상품 URL은 필수입니다.")
    private String productUrl;
}