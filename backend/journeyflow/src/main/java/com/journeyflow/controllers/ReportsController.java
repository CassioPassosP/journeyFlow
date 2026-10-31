package com.journeyflow.controllers;

import com.journeyflow.dto.Reports.ReportsCreateRequestDto;
import com.journeyflow.dto.Reports.ReportsResponseDto;
import com.journeyflow.dto.Reports.ReportsUpdateRequestDto;
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

    private final ReportsService reportsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportsResponseDto create(@Valid @RequestBody ReportsCreateRequestDto request) {
        return reportsService.createReport(request);
    }

    @GetMapping
    public List<ReportsResponseDto> findAll() {
        return reportsService.findAllReports();
    }

    @GetMapping("/{id}")
    public ReportsResponseDto findById(@PathVariable Long id) {
        return reportsService.findReportById(id);
    }

    @PutMapping("/{id}")
    public ReportsResponseDto update(@PathVariable Long id, @Valid @RequestBody ReportsUpdateRequestDto request) {
        return reportsService.updateReport(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reportsService.deleteReport(id);
    }
}