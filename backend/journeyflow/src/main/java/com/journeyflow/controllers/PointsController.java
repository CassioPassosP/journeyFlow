package com.journeyflow.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.dto.Points.PointsBalanceResponseDto;
import com.journeyflow.dto.Points.PointsCreateRequestDto;
import com.journeyflow.dto.Points.PointsResponseDto;
import com.journeyflow.dto.Points.PointsUpdateRequestDto;
import com.journeyflow.services.PointsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PointsResponseDto create(@Valid @RequestBody PointsCreateRequestDto request) {
        return pointsService.createPoints(request);
    }

    /** GET /points?employeeId=...&page=0&size=20 (mais recentes primeiro). */
    @GetMapping
    public PageResponseDto<PointsResponseDto> findAll(
            @RequestParam(required = false) UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return pointsService.findPointsEntries(employeeId, pageable);
    }

    @GetMapping("/{id}")
    public PointsResponseDto findById(@PathVariable Long id) {
        return pointsService.findPointsById(id);
    }

    @GetMapping("/employee/{employeeId}/balance")
    public PointsBalanceResponseDto balance(@PathVariable UUID employeeId) {
        return pointsService.getBalance(employeeId);
    }

    @PutMapping("/{id}")
    public PointsResponseDto update(@PathVariable Long id, @Valid @RequestBody PointsUpdateRequestDto request) {
        return pointsService.updatePoints(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        pointsService.deletePoints(id);
    }
}