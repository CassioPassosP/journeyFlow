package com.journeyflow.services;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.PillProgress.PillProgressCreateRequestDto;
import com.journeyflow.dto.PillProgress.PillProgressResponseDto;
import com.journeyflow.dto.PillProgress.PillProgressUpdateRequestDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.PillProgressEntity;
import com.journeyflow.models.PillProgressId;
import com.journeyflow.models.PillsEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.PillProgressRepository;
import com.journeyflow.repositories.PillsRepository;

import lombok.RequiredArgsConstructor;

/**
 * Progresso = "colaborador X concluiu a pílula Y". A chave é composta (user_id, pill_id),
 * e a conclusão é registrada uma única vez (409 se repetir).
 */
@Service
@RequiredArgsConstructor
public class PillProgressService {

    private final PillProgressRepository pillProgressRepository;
    private final EmployeeRepository employeeRepository;
    private final PillsRepository pillsRepository;

    @Transactional
    public PillProgressResponseDto createProgress(PillProgressCreateRequestDto dto) {
        EmployeeEntity employee = findEmployee(dto.employeeId());
        PillsEntity pill = findPill(dto.pillId());

        if (pillProgressRepository.existsById(new PillProgressId(employee.getId(), pill.getId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Progresso da pílula já existe");
        }
        return save(employee, pill);
    }

    /**
     * Como a chave É o par (colaborador, pílula), "atualizar" significa mover o progresso para
     * outro par: remove o registro atual e cria o novo (completedAt é reiniciado).
     */
    @Transactional
    public PillProgressResponseDto updateProgress(UUID employeeId, Long pillId, PillProgressUpdateRequestDto dto) {
        PillProgressEntity current = findProgress(employeeId, pillId);

        EmployeeEntity newEmployee = findEmployee(dto.employeeId());
        PillsEntity newPill = findPill(dto.pillId());
        PillProgressId newId = new PillProgressId(newEmployee.getId(), newPill.getId());

        if (current.getId().equals(newId)) {
            return PillProgressResponseDto.from(current);
        }
        if (pillProgressRepository.existsById(newId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Progresso de destino já existe");
        }

        pillProgressRepository.delete(current);
        pillProgressRepository.flush();

        return save(newEmployee, newPill);
    }

    @Transactional
    public void deleteProgress(UUID employeeId, Long pillId) {
        pillProgressRepository.delete(findProgress(employeeId, pillId));
    }

    /** Com employeeId: pílulas concluídas por ele (mais recentes primeiro). Sem: todos os registros. */
    @Transactional(readOnly = true)
    public List<PillProgressResponseDto> findAllProgress(UUID employeeId) {
        List<PillProgressEntity> result;

        if (employeeId == null) {
            result = pillProgressRepository.findAll();
        } else {
            if (!employeeRepository.existsById(employeeId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado");
            }
            result = pillProgressRepository.listByEmployeeId(employeeId);
        }
        return result.stream().map(PillProgressResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public PillProgressResponseDto findProgressById(UUID employeeId, Long pillId) {
        return PillProgressResponseDto.from(findProgress(employeeId, pillId));
    }

    // ---------------------------------------------------------------- helpers

    private PillProgressEntity findProgress(UUID employeeId, Long pillId) {
        return pillProgressRepository.findById(new PillProgressId(employeeId, pillId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Progresso da pílula não encontrado"));
    }

    private EmployeeEntity findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    private PillsEntity findPill(Long pillId) {
        return pillsRepository.findById(pillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pílula não encontrada"));
    }

    // saveAndFlush: a chave é atribuída (INSERT adiado), então forçamos o INSERT para ter completedAt preenchido.
    private PillProgressResponseDto save(EmployeeEntity employee, PillsEntity pill) {
        PillProgressEntity progress = new PillProgressEntity();
        progress.setId(new PillProgressId(employee.getId(), pill.getId()));
        progress.setEmployee(employee);
        progress.setPill(pill);

        return PillProgressResponseDto.from(pillProgressRepository.saveAndFlush(progress));
    }
}