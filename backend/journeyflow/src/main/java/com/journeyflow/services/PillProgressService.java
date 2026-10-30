package com.journeyflow.services;

import com.journeyflow.dto.PillProgress.PillProgressCreateRequestDto;
import com.journeyflow.dto.PillProgress.PillProgressResponseDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.PillProgressEntity;
import com.journeyflow.models.PillProgressId;
import com.journeyflow.models.PillsEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.PillProgressRepository;
import com.journeyflow.repositories.PillsRepository;
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
public class PillProgressService {

    private final PillProgressRepository pillProgressRepository;
    private final EmployeeRepository employeeRepository;
    private final PillsRepository pillsRepository;

    @Transactional
    public PillProgressResponseDto createProgress(PillProgressCreateRequestDto dto) {
        EmployeeEntity employee = findEmployee(dto.employeeId());
        PillsEntity pill = findPill(dto.pillId());
        PillProgressId id = new PillProgressId(employee.getId(), pill.getId());

        if (pillProgressRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Progresso da pilula ja existe");
        }

        return new PillProgressResponseDto(pillProgressRepository.save(newProgress(employee, pill)));
    }

    @Transactional
    public PillProgressResponseDto updateProgress(
            UUID employeeId,
            Long pillId,
            @Valid PillProgressCreateRequestDto dto
    ) {
        PillProgressId currentId = new PillProgressId(employeeId, pillId);
        PillProgressEntity currentProgress = pillProgressRepository.findById(currentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Progresso da pilula nao encontrado"
                ));

        EmployeeEntity newEmployee = findEmployee(dto.employeeId());
        PillsEntity newPill = findPill(dto.pillId());
        PillProgressId newId = new PillProgressId(newEmployee.getId(), newPill.getId());

        if (currentId.equals(newId)) {
            return new PillProgressResponseDto(currentProgress);
        }
        if (pillProgressRepository.existsById(newId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Progresso de destino ja existe");
        }

        pillProgressRepository.delete(currentProgress);
        pillProgressRepository.flush();

        return new PillProgressResponseDto(pillProgressRepository.save(newProgress(newEmployee, newPill)));
    }

    @Transactional
    public void deleteProgress(UUID employeeId, Long pillId) {
        PillProgressId id = new PillProgressId(employeeId, pillId);
        PillProgressEntity progress = pillProgressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Progresso da pilula nao encontrado"
                ));

        pillProgressRepository.delete(progress);
    }

    @Transactional
    public List<PillProgressResponseDto> findAllProgress() {
        return pillProgressRepository.findAll()
                .stream()
                .map(PillProgressResponseDto::new)
                .toList();
    }

    @Transactional
    public PillProgressResponseDto findProgressById(UUID employeeId, Long pillId) {
        PillProgressId id = new PillProgressId(employeeId, pillId);
        PillProgressEntity progress = pillProgressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Progresso da pilula nao encontrado"
                ));

        return new PillProgressResponseDto(progress);
    }

    private EmployeeEntity findEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID do funcionario e obrigatorio");
        }
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Funcionario nao encontrado"
                ));
    }

    private PillsEntity findPill(Long pillId) {
        if (pillId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID da pilula e obrigatorio");
        }
        return pillsRepository.findById(pillId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pilula nao encontrada"
                ));
    }

    private PillProgressEntity newProgress(EmployeeEntity employee, PillsEntity pill) {
        PillProgressEntity progress = new PillProgressEntity();
        progress.setId(new PillProgressId(employee.getId(), pill.getId()));
        progress.setEmployee(employee);
        progress.setPill(pill);
        return progress;
    }
}