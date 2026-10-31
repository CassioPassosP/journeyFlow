package com.journeyflow.dto.Pills;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PillsUpdateRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Integer day_from,
        @NotBlank String quiz_question,
        @NotBlank String quiz_options,
        @NotNull Integer quiz_correct
) {}