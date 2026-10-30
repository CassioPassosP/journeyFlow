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
        PillsEntity pill = new PillsEntity();
        applyFields(pill, dto.title(), dto.content(), dto.day_from(),
                dto.quiz_question(), dto.quiz_options(), dto.quiz_correct());

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
        PillsEntity pill = pillsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pilula nao encontrada"
                ));

        return new PillsResponseDto(pill);
    }

    // Copia para a pilula os campos recebidos no DTO.
    private void applyFields(
            PillsEntity pill,
            String title,
            String content,
            Integer dayFrom,
            String quizQuestion,
            String quizOptions,
            Integer quizCorrect
    ) {
        pill.setTitle(title);
        pill.setContent(content);
        pill.setDay_from(dayFrom);
        pill.setQuiz_question(quizQuestion);
        pill.setQuiz_options(quizOptions);
        pill.setQuiz_correct(quizCorrect);
    }
}