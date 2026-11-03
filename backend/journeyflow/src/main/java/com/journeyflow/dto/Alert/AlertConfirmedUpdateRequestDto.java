package com.journeyflow.dto.Alert;

import jakarta.validation.constraints.NotNull;

public record AlertConfirmedUpdateRequestDto(@NotNull Boolean confirmed) {}
