package com.journeyflow.controllers;

import com.journeyflow.dto.Pills.PillsCreateRequestDto;
import com.journeyflow.dto.Pills.PillsResponseDto;
import com.journeyflow.dto.Pills.PillsUpdateRequestDto;
import com.journeyflow.services.PillsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pills")
@RequiredArgsConstructor
public class PillsController {

    private final PillsService pillsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PillsResponseDto create(@Valid @RequestBody PillsCreateRequestDto request) {
        return pillsService.createPill(request);
    }

    @GetMapping
    public List<PillsResponseDto> findAll() {
        return pillsService.findAllPills();
    }

    @GetMapping("/{id}")
    public PillsResponseDto findById(@PathVariable Long id) {
        return pillsService.findPillById(id);
    }

    @PutMapping("/{id}")
    public PillsResponseDto update(@PathVariable Long id, @Valid @RequestBody PillsUpdateRequestDto request) {
        return pillsService.updatePill(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        pillsService.deletePill(id);
    }
}