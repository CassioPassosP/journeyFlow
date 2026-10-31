package com.journeyflow.controllers;

import com.journeyflow.dto.Points.PointsCreateRequestDto;
import com.journeyflow.dto.Points.PointsResponseDto;
import com.journeyflow.dto.Points.PointsUpdateRequestDto;
import com.journeyflow.services.PointsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public List<PointsResponseDto> findAll() {
        return pointsService.findAllPoints();
    }

    @GetMapping("/{id}")
    public PointsResponseDto findById(@PathVariable Long id) {
        return pointsService.findPointsById(id);
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