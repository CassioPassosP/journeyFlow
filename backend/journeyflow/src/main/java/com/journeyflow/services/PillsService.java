package com.journeyflow.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Pills.PillsCreateRequestDto;
import com.journeyflow.dto.Pills.PillsResponseDto;
import com.journeyflow.dto.Pills.PillsUpdateRequestDto;
import com.journeyflow.models.PillsEntity;
import com.journeyflow.repositories.PillsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PillsService {

    private static final Sort DEFAULT_SORT = Sort.by("id");

    private final PillsRepository pillsRepository;

    @Transactional
    public PillsResponseDto createPill(PillsCreateRequestDto dto) {
        PillsEntity pill = new PillsEntity();
        apply(pill, dto.title(), dto.content(), dto.dayFrom(), dto.quizQuestion(), dto.quizOptions(), dto.quizCorrect());

        return PillsResponseDto.from(pillsRepository.save(pill));
    }

    @Transactional
    public PillsResponseDto updatePill(Long id, PillsUpdateRequestDto dto) {
        PillsEntity pill = findPill(id);
        apply(pill, dto.title(), dto.content(), dto.dayFrom(), dto.quizQuestion(), dto.quizOptions(), dto.quizCorrect());

        return PillsResponseDto.from(pill);
    }

    @Transactional
    public void deletePill(Long id) {
        pillsRepository.delete(findPill(id));
        pillsRepository.flush(); // pílula já concluída por alguém (FK em pill_progress) vira 409
    }

    @Transactional(readOnly = true)
    public List<PillsResponseDto> findAllPills() {
        return pillsRepository.findAll(DEFAULT_SORT)
                .stream()
                .map(PillsResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PillsResponseDto findPillById(Long id) {
        return PillsResponseDto.from(findPill(id));
    }

    private PillsEntity findPill(Long id) {
        return pillsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pílula não encontrada"));
    }

    // Create e update gravavam os mesmos 6 campos em dois blocos idênticos; agora há um só. 
    private static void apply(PillsEntity pill, String title, String content, Integer dayFrom,
                              String quizQuestion, String quizOptions, Integer quizCorrect) {
        pill.setTitle(title.trim());
        pill.setContent(content);
        pill.setDay_from(dayFrom);               // a entidade ainda usa snake_case (day_from)
        pill.setQuiz_question(quizQuestion);
        pill.setQuiz_options(quizOptions);
        pill.setQuiz_correct(quizCorrect);
    }
}