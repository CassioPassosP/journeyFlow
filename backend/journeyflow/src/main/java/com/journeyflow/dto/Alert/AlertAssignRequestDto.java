package com.journeyflow.dto.Alert;

import jakarta.validation.constraints.NotNull;

public record AlertAssignRequestDto(@NotNull Long managerId) {}
