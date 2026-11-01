package com.journeyflow.dto.Employee;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.journeyflow.models.EmployeeEntity;

public record EmployeeResponseDto(
        UUID id,
        Long storeId,
        String name,
        String email,
        String position,
        OffsetDateTime createdAt,
        String currentState,
        boolean inLearningPath,
        OffsetDateTime lastInteraction,
        OffsetDateTime registerDate,
        LocalDate startDate,
        boolean consent,
        OffsetDateTime consentAt,
        int streak,
        LocalDate lastActive
) {
    public static EmployeeResponseDto from(EmployeeEntity e) {
        return new EmployeeResponseDto(
                e.getId(),
                e.getStore().getId(), // getId() em proxy LAZY não dispara SELECT
                e.getName(),
                e.getEmail(),
                e.getPosition(),
                e.getCreatedAt(),
                e.getCurrentState(),
                e.isInLearningPath(),
                e.getLastInteraction(),
                e.getRegisterDate(),
                e.getStartDate(),
                e.isConsent(),
                e.getConsentAt(),
                e.getStreak(),
                e.getLastActive());
    }
}