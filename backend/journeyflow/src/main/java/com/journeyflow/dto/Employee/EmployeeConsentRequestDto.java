package com.journeyflow.dto.Employee;

import jakarta.validation.constraints.NotNull;

public record EmployeeConsentRequestDto(@NotNull Boolean consent) {}
