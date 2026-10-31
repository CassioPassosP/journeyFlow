package com.journeyflow.services;

import com.journeyflow.dto.Pills.PillsCreateRequestDto;
import com.journeyflow.dto.Pills.PillsResponseDto;
import com.journeyflow.dto.Pills.PillsUpdateRequestDto;
import com.journeyflow.models.PillsEntity;
import com.journeyflow.repositories.PillsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class PillsService {

    private final PillsRepository pillsRepository;

    @Transactional
    public PillsResponseDto createPill(PillsCreateRequestDto dto) {
        validate(dto.title(), dto.content(), dto.day_from(), dto.quiz_question(), dto.quiz_options(), dto.quiz_correct());

        PillsEntity pill = new PillsEntity();
        pill.setTitle(dto.title());
        pill.setContent(dto.content());
        pill.setDay_from(dto.day_from());
        pill.setQuiz_question(dto.quiz_question());
        pill.setQuiz_options(dto.quiz_options());
        pill.setQuiz_correct(dto.quiz_correct());

        return new PillsResponseDto(pillsRepository.save(pill));
    }

    @Transactional
    public PillsResponseDto updatePill(Long id, PillsUpdateRequestDto dto) {
        PillsEntity pill = pillsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pilula nao encontrada"
                ));
        validate(dto.title(), dto.content(), dto.day_from(), dto.quiz_question(), dto.quiz_options(), dto.quiz_correct());

        pill.setTitle(dto.title());
        pill.setContent(dto.content());
        pill.setDay_from(dto.day_from());
        pill.setQuiz_question(dto.quiz_question());
        pill.setQuiz_options(dto.quiz_options());
        pill.setQuiz_correct(dto.quiz_correct());

        return new PillsResponseDto(pillsRepository.save(pill));
    }

    @Transactional
    public void deletePill(Long id) {
        PillsEntity pill = pillsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pilula nao encontrada"
                ));
        pillsRepository.delete(pill);
    }

    @Transactional
    public List<PillsResponseDto> findAllPills() {
        return pillsRepository.findAll()
                .stream()
                .map(PillsResponseDto::new)
                .toList();
    }

    @Transactional
    public PillsResponseDto findPillById(Long id) {
        return pillsRepository.findById(id)
                .map(PillsResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pilula nao encontrada"
                ));
    }

    private void validate(String title, String content, Integer dayFrom, String quizQuestion,
                          String quizOptions, Integer quizCorrect) {
        if (title == null || title.isBlank() || content == null || content.isBlank() || dayFrom == null
                || quizQuestion == null || quizQuestion.isBlank() || quizOptions == null || quizOptions.isBlank()
                || quizCorrect == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Todos os campos da pilula sao obrigatorios"
            );
        }
    }
}