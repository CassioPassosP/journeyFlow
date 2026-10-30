package com.journeyflow.dto.Pills;

import com.journeyflow.models.PillsEntity;

public record PillsResponseDto(
        Long id,
        String title,
        String content,
        Integer day_from,
        String quiz_question,
        String quiz_options,
        Integer quiz_correct
) {
    public PillsResponseDto(PillsEntity pill) {
        this(
                pill.getId(),
                pill.getTitle(),
                pill.getContent(),
                pill.getDay_from(),
                pill.getQuiz_question(),
                pill.getQuiz_options(),
                pill.getQuiz_correct()
        );
    }
}
