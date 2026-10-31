package com.journeyflow.controllers;

import com.journeyflow.dto.PillProgress.PillProgressCreateRequestDto;
import com.journeyflow.dto.PillProgress.PillProgressResponseDto;
import com.journeyflow.dto.PillProgress.PillProgressUpdateRequestDto;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PillProgressResponseDto create(@Valid @RequestBody PillProgressCreateRequestDto request) {
        return pillProgressService.createProgress(request);
    }

    @GetMapping
    public List<PillProgressResponseDto> findAll() {
        return pillProgressService.findAllProgress();
    }

    @GetMapping("/{employeeId}/{pillId}")
    public PillProgressResponseDto findById(@PathVariable UUID employeeId, @PathVariable Long pillId) {
        return pillProgressService.findProgressById(employeeId, pillId);
    }

    @PutMapping("/{employeeId}/{pillId}")
    public PillProgressResponseDto update(@PathVariable UUID employeeId, @PathVariable Long pillId,
                                          @Valid @RequestBody PillProgressUpdateRequestDto request) {
        return pillProgressService.updateProgress(employeeId, pillId, request);
    }

    @DeleteMapping("/{employeeId}/{pillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID employeeId, @PathVariable Long pillId) {
        pillProgressService.deleteProgress(employeeId, pillId);
    }
}