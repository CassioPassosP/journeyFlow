package com.journeyflow.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.dto.Reports.ReportsCreateRequestDto;
import com.journeyflow.dto.Reports.ReportsResponseDto;
import com.journeyflow.dto.Reports.ReportsStatusUpdateRequestDto;
import com.journeyflow.dto.Reports.ReportsUpdateRequestDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.ReportsEntity;
import com.journeyflow.models.enums.ReportStatus;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.ReportsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ReportsRepository reportsRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * editedText é opcional na criação: quando ausente repete o texto original.
     * O status nasce com o default da entidade (ANALYZED) e só muda via updateStatus.
     */
    
    @Transactional
    public ReportsResponseDto createReport(ReportsCreateRequestDto dto) {
        EmployeeEntity employee = findEmployee(dto.employeeId());

        ReportsEntity report = new ReportsEntity();
        report.setEmployeeId(employee.getId());
        report.setOriginalText(dto.originalText());
        report.setEditedText(dto.editedText() == null || dto.editedText().isBlank()
                ? dto.originalText()
                : dto.editedText());
        report.setSummary(dto.summary());
        report.setTopic(dto.topic());
        report.setKind(dto.kind());
        if (dto.source() != null) report.setSource(dto.source());
        report.setHelpRequest(dto.helpRequest());

        return ReportsResponseDto.from(reportsRepository.save(report));
    }

    @Transactional
    public ReportsResponseDto updateReport(Long id, ReportsUpdateRequestDto dto) {
        ReportsEntity report = findReport(id);
        EmployeeEntity employee = findEmployee(dto.employeeId());

        report.setEmployeeId(employee.getId());
        report.setOriginalText(dto.originalText());
        report.setEditedText(dto.editedText());
        report.setSummary(dto.summary());
        report.setTopic(dto.topic());
        report.setKind(dto.kind());
        if (dto.source() != null) report.setSource(dto.source());
        if (dto.status() != null) report.setStatus(dto.status());
        report.setHelpRequest(dto.helpRequest());

        return ReportsResponseDto.from(report);
    }

    @Transactional
    public ReportsResponseDto updateStatus(Long id, ReportsStatusUpdateRequestDto dto) {
        ReportsEntity report = findReport(id);
        report.setStatus(dto.status());

        return ReportsResponseDto.from(report);
    }

    @Transactional
    public void deleteReport(Long id) {
        reportsRepository.delete(findReport(id));
        reportsRepository.flush(); // relato com alertas (FK em alerts) vira 409
    }

    @Transactional(readOnly = true)
    public PageResponseDto<ReportsResponseDto> findReports(UUID employeeId, ReportStatus status, Pageable pageable) {
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

        Page<ReportsEntity> result;
        if (employeeId != null && status != null) {
            result = reportsRepository.findByEmployeeIdAndStatus(employeeId, status, page);
        } else if (employeeId != null) {
            result = reportsRepository.findByEmployeeId(employeeId, page);
        } else if (status != null) {
            result = reportsRepository.findByStatus(status, page);
        } else {
            result = reportsRepository.findAll(page);
        }
        return PageResponseDto.from(result, ReportsResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ReportsResponseDto findReportById(Long id) {
        return ReportsResponseDto.from(findReport(id));
    }

    private ReportsEntity findReport(Long id) {
        return reportsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relato não encontrado"));
    }

    private EmployeeEntity findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }
}