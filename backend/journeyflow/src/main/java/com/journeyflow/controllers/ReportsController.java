package com.journeyflow.controllers;

import com.journeyflow.dto.Points.PointsCreateRequestDto;
import com.journeyflow.dto.Points.PointsResponseDto;
import com.journeyflow.dto.Reports.ReportsCreateRequestDto;
import com.journeyflow.dto.Reports.ReportsResponseDto;
import com.journeyflow.services.ReportsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService  reportsService;

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportsResponseDto create(
            @Valid @RequestBody ReportsCreateRequestDto request) {

        return reportsService.createReport(request);
    }

    //find all
    @GetMapping
    public List<ReportsResponseDto> findAll() {

        return reportsService.findAllReports();
    }

    //find by id
    @GetMapping("/{id}")
    public ReportsResponseDto findById(@PathVariable Long id) {

        return reportsService.findReportById(id);
    }

    //update
    @PutMapping("/{id}")
    public ReportsResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody ReportsCreateRequestDto request) {

        return reportsService.updateReport(id, request);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        reportsService.deleteReport(id);
    }

}
