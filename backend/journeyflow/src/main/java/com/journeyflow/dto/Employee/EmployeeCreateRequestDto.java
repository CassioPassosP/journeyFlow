package com.journeyflow.dto.Employee;

import com.journeyflow.models.ChatEntity;
import com.journeyflow.models.PillsEntity;
import com.journeyflow.models.PointsEntity;
import com.journeyflow.models.StoreEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.Date;

public record EmployeeCreateRequestDto(
        @NotNull StoreEntity store,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String position,
        PointsEntity points,
        ChatEntity messages,
        PillsEntity pilss,
        @NotNull Date initiationDate,
        @NotBlank String currentState,
        boolean inLearningPath,
        OffsetDateTime lastInteraction,
        OffsetDateTime registerDate,
        @NotNull Date startDate,
        boolean consent,
        OffsetDateTime consentAt,
        int streak,
        @NotNull Date lastActive
) {}