package com.journeyflow.dto.Reports;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.ReportKind;
import com.journeyflow.models.enums.ReportStatus;

import java.util.UUID;

public record ReportsUpdateRequestDto(
        UUID employee,
        String originalText,
        String editedText,
        String summary,
        String topic,
        ReportKind kind,
        ContentSource source,
        ReportStatus status,
        boolean helpRequest
) {}