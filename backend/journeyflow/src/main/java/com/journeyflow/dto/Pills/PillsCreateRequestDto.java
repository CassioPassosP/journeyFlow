package com.journeyflow.dto.Pills;

public record PillsCreateRequestDto(
        String title,
        String content,
        Integer day_from,
        String quiz_question,
        String quiz_options,
        Integer quiz_correct
) {}
