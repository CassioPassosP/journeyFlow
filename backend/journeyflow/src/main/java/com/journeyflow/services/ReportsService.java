package com.journeyflow.services;

import com.journeyflow.dto.Reports.ReportsCreateRequestDto;
import com.journeyflow.dto.Reports.ReportsResponseDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.ReportsEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.ReportsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ReportsService {

    private final ReportsRepository reportsRepository;
    private final EmployeeRepository employeeRepository;

    //create
    @Transactional
    public ReportsResponseDto createReport(ReportsCreateRequestDto dto) {
        if (dto.employee() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Funcionário é obrigatório");
        }

        EmployeeEntity employee = employeeRepository.findById(dto.employee())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado"
                ));

        ReportsEntity report = new ReportsEntity();
        report.setEmployee(employee);
        report.setOriginalText(dto.originalText());
        report.setEditedText(dto.editedText());
        report.setSummary(dto.summary());
        report.setTopic(dto.topic());
        report.setKind(dto.kind());

        if (dto.source() != null) {
            report.setSource(dto.source());
        }
        if (dto.status() != null) {
            report.setStatus(dto.status());
        }

        report.setHelpRequest(dto.helpRequest());

        ReportsEntity savedReport = reportsRepository.save(report);
        return new ReportsResponseDto(savedReport);
    }

    //find all
    @Transactional
    public List<ReportsResponseDto> findAllReports(){
        return reportsRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Converte Entity para Response DTO
    private ReportsResponseDto toResponse(ReportsEntity reports) {

        return new ReportsResponseDto(
                reports.getId(),
                reports.getEmployee().getId(),
                reports.getOriginalText(),
                reports.getEditedText(),
                reports.getSummary(),
                reports.getTopic(),
                reports.getKind() != null ? reports.getKind().name() : null,
                reports.getSource().name(),
                reports.getStatus().name(),
                reports.isHelpRequest(),
                reports.getCreatedAt()
        );
    }
}

