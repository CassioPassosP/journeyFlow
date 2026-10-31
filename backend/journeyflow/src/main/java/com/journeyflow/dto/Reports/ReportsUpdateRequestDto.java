package com.journeyflow.dto.Reports;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.ReportKind;
import com.journeyflow.models.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReportsUpdateRequestDto(
        @NotNull UUID employee,
        @NotBlank String originalText,
        @NotBlank String editedText,
        String summary,
        String topic,
        ReportKind kind,
        ContentSource source,
        ReportStatus status,
        boolean helpRequest
) {}