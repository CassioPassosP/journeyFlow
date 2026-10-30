package com.journeyflow.services;

import com.journeyflow.dto.Reports.ReportsCreateRequestDto;
import com.journeyflow.dto.Reports.ReportsResponseDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.ReportsEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.ReportsRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReportsService {

    private final ReportsRepository reportsRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ReportsResponseDto createReport(ReportsCreateRequestDto dto) {
        validateRequiredText(dto.originalText(), dto.editedText());
        EmployeeEntity employee = findEmployee(dto.employee());

        ReportsEntity report = new ReportsEntity();

        report.setEmployee(employee);
        report.setOriginalText(dto.originalText());
        report.setEditedText(dto.editedText());
        report.setSummary(dto.summary());
        report.setTopic(dto.topic());
        report.setKind(dto.kind());
        if (dto.source() != null) report.setSource(dto.source());
        if (dto.status() != null) report.setStatus(dto.status());
        report.setHelpRequest(dto.helpRequest());

        return new ReportsResponseDto(reportsRepository.save(report));
    }

    @Transactional
    public ReportsResponseDto updateReport(Long id, @Valid ReportsCreateRequestDto dto) {
        ReportsEntity report = reportsRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Relatorio nao encontrado"));

        validateRequiredText(dto.originalText(), dto.editedText());
        report.setEmployee(findEmployee(dto.employee()));
        report.setOriginalText(dto.originalText());
        report.setEditedText(dto.editedText());
        report.setSummary(dto.summary());
        report.setTopic(dto.topic());
        report.setKind(dto.kind());
        if (dto.source() != null) report.setSource(dto.source());
        if (dto.status() != null) report.setStatus(dto.status());
        report.setHelpRequest(dto.helpRequest());

        return new ReportsResponseDto(reportsRepository.save(report));
    }

    @Transactional
    public void deleteReport(Long id) {
        ReportsEntity report = reportsRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Relatorio nao encontrado"));

        reportsRepository.delete(report);
    }

    @Transactional
    public List<ReportsResponseDto> findAllReports() {
        return reportsRepository.findAll()
                .stream()
                .map(ReportsResponseDto::new)
                .toList();
    }

    @Transactional
    public ReportsResponseDto findReportById(Long id) {
        return reportsRepository.findById(id)
                .map(ReportsResponseDto::new)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Relatorio nao encontrado"));
    }

    private EmployeeEntity findEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID do funcionario e obrigatorio");
        }
        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Funcionario nao encontrado"));
    }

    private void validateRequiredText(String originalText, String editedText) {
        if (originalText == null || originalText.isBlank() || editedText == null || editedText.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Texto original e texto editado sao obrigatorios");
        }
    }
}