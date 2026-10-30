package com.journeyflow.controllers;

import com.journeyflow.dto.Pills.PillsResponseDto;
import com.journeyflow.dto.Reports.ReportsCreateRequestDto;
import com.journeyflow.dto.Reports.ReportsResponseDto;
import com.journeyflow.services.PillsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pills")
@RequiredArgsConstructor
public class PillsController {

    private final PillsService pillsService;

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PillsResponseDto create(
            @Valid @RequestBody ReportsCreateRequestDto request) {

        return pillsService.createPill(request);
    }

    //find all
    @GetMapping
    public List<PillsResponseDto> findAll() {

        return pillsService.findAllPills();
    }

    //find by id
    @GetMapping("/{id}")
    public PillsResponseDto findById(@PathVariable Long id) {

        return pillsService.findPillById(id);
    }

    //update
    @PutMapping("/{id}")
    public PillsResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody ReportsCreateRequestDto request) {

        return pillsService.updatePill(id, request);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        pillsService.deletePill(id);
    }

}
