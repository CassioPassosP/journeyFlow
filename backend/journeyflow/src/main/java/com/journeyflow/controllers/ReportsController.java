package com.journeyflow.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.dto.Reports.ReportsCreateRequestDto;
import com.journeyflow.dto.Reports.ReportsResponseDto;
import com.journeyflow.dto.Reports.ReportsStatusUpdateRequestDto;
import com.journeyflow.dto.Reports.ReportsUpdateRequestDto;
import com.journeyflow.models.enums.ReportStatus;
import com.journeyflow.services.ReportsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    /** GET /reports?employeeId=...&status=ANALYZED&page=0&size=20 (mais recentes primeiro). */
    @GetMapping
    public PageResponseDto<ReportsResponseDto> findAll(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return reportsService.findReports(employeeId, status, pageable);
    }

    @GetMapping("/{id}")
    public ReportsResponseDto findById(@PathVariable Long id) {
        return reportsService.findReportById(id);
    }

    @PutMapping("/{id}")
    public ReportsResponseDto update(@PathVariable Long id, @Valid @RequestBody ReportsUpdateRequestDto request) {
        return reportsService.updateReport(id, request);
    }

    @PatchMapping("/{id}/status")
    public ReportsResponseDto updateStatus(@PathVariable Long id,
                                           @Valid @RequestBody ReportsStatusUpdateRequestDto request) {
        return reportsService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reportsService.deleteReport(id);
    }
}