package com.journeyflow.dto.Employee;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Consentimento, streak e atividade têm operações próprias no service (regras, não campos livres). */
public record EmployeeUpdateRequestDto(
        @NotNull Long storeId,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String position,
        @NotNull LocalDate initiationDate,
        @NotNull LocalDate startDate,
        @NotBlank @Size(max = 50) String currentState,
        @NotNull Boolean inLearningPath
) {}