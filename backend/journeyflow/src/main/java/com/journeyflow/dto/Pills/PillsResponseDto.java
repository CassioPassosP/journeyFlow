package com.journeyflow.dto.Pills;

import com.journeyflow.models.PillsEntity;

public record PillsResponseDto(
        Long id,
        String title,
        String content,
        Integer dayFrom,
        String quizQuestion,
        String quizOptions,
        Integer quizCorrect
) {
    public static PillsResponseDto from(PillsEntity p) {
        return new PillsResponseDto(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                p.getDayFrom(),
                p.getQuiz_question(),
                p.getQuiz_options(),
                p.getQuiz_correct());
    }
}
