package com.journeyflow.dto.Employee;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeCreateRequestDto(
        @NotNull Long storeId,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 100) String position,
        @NotNull LocalDate initiationDate,
        @NotNull LocalDate startDate,
        @NotBlank @Size(max = 50) String currentState,
        @NotNull Boolean inLearningPath,
        @NotNull Boolean consent
) {
    @Override
    public String toString() {
        return "EmployeeCreateRequestDto[storeId=%s, name=%s, email=%s, password=***]".formatted(storeId, name, email);
    }
}