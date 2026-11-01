package com.journeyflow.dto.Reports;

import java.util.UUID;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.ReportKind;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportsCreateRequestDto(
        @NotNull UUID employeeId,
        @NotBlank String originalText,
        String editedText,          // opcional: se ausente, repete originalText
        @Size(max = 1000) String summary,
        @Size(max = 200) String topic,
        ReportKind kind,
        ContentSource source,       // opcional: default TEXT
        boolean helpRequest
) {}