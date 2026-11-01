package com.journeyflow.dto.Alert;

import com.journeyflow.models.enums.AlertStatus;

import jakarta.validation.constraints.NotNull;

public record AlertStatusUpdateRequestDto(@NotNull AlertStatus status) {}