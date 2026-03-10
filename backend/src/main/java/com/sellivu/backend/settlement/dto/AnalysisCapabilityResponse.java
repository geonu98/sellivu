package com.sellivu.backend.settlement.dto;

import com.sellivu.backend.settlement.domain.AnalysisGapType;
import com.sellivu.backend.settlement.domain.SettlementFileType;
import com.sellivu.backend.settlement.domain.ViewType;

import java.util.List;

public class AnalysisCapabilityResponse {

    private final List<SettlementFileType> uploadedFileTypes;
    private final List<ViewType> availableViews;
    private final List<SettlementFileType> missingFileTypes;
    private final List<AnalysisGapType> gaps;
    private final boolean requiresContextOptions;
    private final List<String> explainablePolicyFactors;
    private final List<String> verificationPendingFields;
    private final String message;

    public AnalysisCapabilityResponse(
            List<SettlementFileType> uploadedFileTypes,
            List<ViewType> availableViews,
            List<SettlementFileType> missingFileTypes,
            List<AnalysisGapType> gaps,
            boolean requiresContextOptions,
            List<String> explainablePolicyFactors,
            List<String> verificationPendingFields,
            String message
    ) {
        this.uploadedFileTypes = uploadedFileTypes;
        this.availableViews = availableViews;
        this.missingFileTypes = missingFileTypes;
        this.gaps = gaps;
        this.requiresContextOptions = requiresContextOptions;
        this.explainablePolicyFactors = explainablePolicyFactors;
        this.verificationPendingFields = verificationPendingFields;
        this.message = message;
    }

    public List<SettlementFileType> getUploadedFileTypes() {
        return uploadedFileTypes;
    }

    public List<ViewType> getAvailableViews() {
        return availableViews;
    }

    public List<SettlementFileType> getMissingFileTypes() {
        return missingFileTypes;
    }

    public List<AnalysisGapType> getGaps() {
        return gaps;
    }

    public boolean isRequiresContextOptions() {
        return requiresContextOptions;
    }

    public List<String> getExplainablePolicyFactors() {
        return explainablePolicyFactors;
    }

    public List<String> getVerificationPendingFields() {
        return verificationPendingFields;
    }

    public String getMessage() {
        return message;
    }
}