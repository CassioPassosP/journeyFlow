package com.journeyflow.dto.Alert;

import java.time.LocalDateTime;
import java.util.UUID;

import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertUrgency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Enums tipados, store_id presente e estado inicial controlado pelo service.
public record AlertCreateRequestDto(
        Long reportId,
        UUID employeeId,
        @NotNull Long storeId,
        Long assignedManagerId,
        @NotNull AlertDestination destination,
        String suggestion,
        @NotNull AlertUrgency urgency,
        @NotNull LocalDateTime alertDate,
        @NotBlank @Size(max = 200) String alertTitle,
        @NotBlank @Size(max = 500) String subtitle
) {}