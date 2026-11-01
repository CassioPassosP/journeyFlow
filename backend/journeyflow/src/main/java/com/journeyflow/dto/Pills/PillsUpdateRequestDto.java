package com.journeyflow.dto.Pills;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PillsUpdateRequestDto(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        @NotNull @PositiveOrZero Integer dayFrom,
        @NotBlank String quizQuestion,
        @NotBlank String quizOptions,
        @NotNull @PositiveOrZero Integer quizCorrect
) {}