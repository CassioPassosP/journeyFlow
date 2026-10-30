package com.journeyflow.dto.Employee;

import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.StoreEntity;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

public record EmployeeResponseDto(
        UUID id,
        StoreEntity store,
        String name,
        String email,
        String position,
        OffsetDateTime createdAt,
        Date initiationDate,
        String currentState,
        boolean inLearningPath,
        OffsetDateTime lastInteraction,
        OffsetDateTime registerDate,
        Date startDate,
        boolean consent,
        OffsetDateTime consentAt,
        int streak,
        Date lastActive
) {
    public EmployeeResponseDto(EmployeeEntity employee) {
        this(
                employee.getId(),
                employee.getStore(),
                employee.getName(),
                employee.getEmail(),
                employee.getPosition(),
                employee.getCreatedAt(),
                employee.getInitiationDate(),
                employee.getCurrentState(),
                employee.isInLearningPath(),
                employee.getLastInteraction(),
                employee.getRegisterDate(),
                employee.getStartDate(),
                employee.isConsent(),
                employee.getConsentAt(),
                employee.getStreak(),
                employee.getLastActive()
        );
    }
}