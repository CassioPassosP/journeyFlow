package com.journeyflow.controllers;

import com.journeyflow.dto.Manager.ManagerCreateRequestDto;
import com.journeyflow.dto.Manager.ManagerResponseDto;
import com.journeyflow.dto.Points.PointsCreateRequestDto;
import com.journeyflow.dto.Points.PointsResponseDto;
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

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PointsResponseDto create(
            @Valid @RequestBody PointsCreateRequestDto request) {

        return pointsService.createPoints(request);
    }

    //find all
    @GetMapping
    public List<PointsResponseDto> findAll() {

        return pointsService.findAllPoints();
    }

    //find by id
    @GetMapping("/{id}")
    public PointsResponseDto findById(@PathVariable Long id) {

        return pointsService.findPointsById(id);
    }

    //update
    @PutMapping("/{id}")
    public PointsResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody PointsCreateRequestDto request) {

        return pointsService.updatePoints(id, request);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        pointsService.deletePoints(id);
    }
}
