package com.journeyflow.services;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Alert.AlertAssignRequestDto;
import com.journeyflow.dto.Alert.AlertCreateRequestDto;
import com.journeyflow.dto.Alert.AlertResponseDto;
import com.journeyflow.dto.Alert.AlertStatusUpdateRequestDto;
import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.models.AlertEntity;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.models.ReportsEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.models.enums.AlertStatus;
import com.journeyflow.repositories.AlertRepository;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.ManagerRepository;
import com.journeyflow.repositories.ReportsRepository;
import com.journeyflow.repositories.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final AlertRepository alertRepository;
    private final ReportsRepository reportsRepository;
    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final StoreRepository storeRepository;
    private final Clock clock;

    @Transactional
    public AlertResponseDto createAlert(AlertCreateRequestDto dto) {
        ReportsEntity report = findReport(dto.reportId());
        StoreEntity store = findStore(dto.storeId());
        assertReportBelongsToStore(report, store);

        AlertEntity alert = new AlertEntity();
        alert.setReport(report);
        alert.setStore(store);
        alert.setAssignedTo(dto.assignedManagerId() == null
                ? null
                : findManagerOfStore(dto.assignedManagerId(), store.getId()));
        alert.setDestination(dto.destination());
        alert.setSuggestion(dto.suggestion());
        alert.setUrgency(dto.urgency());
        alert.setAlertDate(dto.alertDate());
        alert.setAlertTitle(dto.alertTitle().trim());
        alert.setSubtitle(dto.subtitle().trim());
        // status = OPEN, reminderSent = false, confirmed = false, answeredAt = null (defaults da entidade)

        return AlertResponseDto.from(alertRepository.save(alert));
    }

    @Transactional
    public AlertResponseDto assignManager(Long id, AlertAssignRequestDto dto) {
        AlertEntity alert = findAlert(id);
        alert.setAssignedTo(findManagerOfStore(dto.managerId(), alert.getStore().getId()));

        return AlertResponseDto.from(alert);
    }

    /**
     * OPEN -> DONE/NOT_DONE registra quando o alerta foi respondido (answeredAt);
     * DONE marca confirmed. Voltar para OPEN limpa os dois.
     */
    @Transactional
    public AlertResponseDto updateStatus(Long id, AlertStatusUpdateRequestDto dto) {
        AlertEntity alert = findAlert(id);
        AlertStatus newStatus = dto.status();

        if (alert.getStatus() != newStatus) {
            alert.setStatus(newStatus);
            if (newStatus == AlertStatus.OPEN) {
                alert.setAnsweredAt(null);
                alert.setConfirmed(false);
            } else {
                alert.setAnsweredAt(OffsetDateTime.now(clock));
                alert.setConfirmed(newStatus == AlertStatus.DONE);
            }
        }
        return AlertResponseDto.from(alert);
    }

    @Transactional
    public void deleteAlert(Long id) {
        alertRepository.delete(findAlert(id));
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AlertResponseDto> findAlerts(Long storeId, AlertStatus status, Pageable pageable) {
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

        Page<AlertEntity> result;
        if (storeId != null && status != null) {
            result = alertRepository.findByStoreIdAndStatus(storeId, status, page);
        } else if (storeId != null) {
            result = alertRepository.findByStoreId(storeId, page);
        } else if (status != null) {
            result = alertRepository.findByStatus(status, page);
        } else {
            result = alertRepository.findAll(page);
        }
        return PageResponseDto.from(result, AlertResponseDto::from);
    }

    @Transactional(readOnly = true)
    public AlertResponseDto findAlertById(Long id) {
        return AlertResponseDto.from(findAlert(id));
    }

    // ---------------------------------------------------------------- helpers

    private AlertEntity findAlert(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta não encontrado"));
    }

    private ReportsEntity findReport(Long id) {
        return reportsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relato não encontrado"));
    }

    private StoreEntity findStore(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja não encontrada"));
    }

    /** O gerente responsável precisa ser da mesma loja do alerta. */
    private ManagerEntity findManagerOfStore(Long managerId, Long storeId) {
        ManagerEntity manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gerente não encontrado"));

        if (!storeId.equals(manager.getStore().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O gerente não pertence à loja do alerta");
        }
        return manager;
    }

    /** A loja do alerta deve ser a loja do colaborador que fez o relato. */
    private void assertReportBelongsToStore(ReportsEntity report, StoreEntity store) {
        EmployeeEntity employee = employeeRepository.findById(report.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Funcionário do relato não encontrado"));

        if (!store.getId().equals(employee.getStore().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A loja informada não é a loja do colaborador que fez o relato");
        }
    }
}