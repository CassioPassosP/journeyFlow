package com.journeyflow.dto.Reports;

import com.journeyflow.models.enums.ReportStatus;

import jakarta.validation.constraints.NotNull;

public record ReportsStatusUpdateRequestDto(@NotNull ReportStatus status) {}
