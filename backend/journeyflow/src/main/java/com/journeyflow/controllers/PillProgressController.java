package com.journeyflow.controllers;

import com.journeyflow.dto.Manager.ManagerCreateRequestDto;
import com.journeyflow.dto.Manager.ManagerResponseDto;
import com.journeyflow.dto.PillProgress.PillProgressCreateRequestDto;
import com.journeyflow.dto.PillProgress.PillProgressResponseDto;
import com.journeyflow.services.PillProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pillprogress")
@RequiredArgsConstructor
public class PillProgressController {

    private final PillProgressService pillProgressService;

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PillProgressResponseDto create(
            @Valid @RequestBody PillProgressCreateRequestDto request) {

        return pillProgressService.createProgress(request);
    }

    //find all
    @GetMapping
    public List<PillProgressResponseDto> findAll() {

        return pillProgressService.findAllProgress();
    }

    //find by id
    @GetMapping("/{id}")
    public PillProgressResponseDto findById(@PathVariable UUID id,
                                            @PathVariable Long pillId) {

        return pillProgressService.findProgressById(id, pillId);
    }

    //update
    @PutMapping("/{id}")
    public PillProgressResponseDto update(
            @PathVariable UUID id,
            @PathVariable Long pillId,
            @Valid @RequestBody PillProgressCreateRequestDto request) {

        return pillProgressService.updateProgress(id, pillId, request);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
            @PathVariable Long pillId) {

        pillProgressService.deleteProgress(id, pillId);
    }

}
