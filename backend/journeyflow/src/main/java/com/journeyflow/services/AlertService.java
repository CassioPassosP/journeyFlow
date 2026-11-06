package com.journeyflow.services;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Alert.AlertAssignRequestDto;
import com.journeyflow.dto.Alert.AlertConfirmedUpdateRequestDto;
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
import com.journeyflow.models.enums.AlertDestination;
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
    public AlertResponseDto createAlert(AlertCreateRequestDto dto, String actorEmail) {
        ManagerEntity actor = requireManager(actorEmail);
        assertDestination(actor, dto.destination());
        if (!actor.getStore().getId().equals(dto.storeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode criar alertas na sua unidade");
        }

        ReportsEntity report = dto.reportId() == null ? null : findReport(dto.reportId());
        if (report == null && dto.employeeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe o funcionário ou um relatório para o alerta");
        }
        EmployeeEntity employee = dto.employeeId() == null
                ? findEmployee(report.getEmployeeId())
                : findEmployee(dto.employeeId());
        if (report != null && !report.getEmployeeId().equals(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O funcionário informado não corresponde ao relatório");
        }
        StoreEntity store = findStore(dto.storeId());
        if (!store.getId().equals(employee.getStore().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A loja informada não é a loja do colaborador do alerta");
        }

        AlertEntity alert = new AlertEntity();
        alert.setReport(report);
        alert.setEmployeeId(employee.getId());
        alert.setStore(store);
        alert.setAssignedTo(dto.assignedManagerId() == null
                ? null
                : findManagerOfStore(dto.assignedManagerId(), store.getId(), dto.destination()));
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
    public AlertResponseDto assignManager(Long id, AlertAssignRequestDto dto, String actorEmail) {
        AlertEntity alert = findAlert(id);
        assertAlertAccess(alert, requireManager(actorEmail));
        alert.setAssignedTo(findManagerOfStore(
                dto.managerId(), alert.getStore().getId(), alert.getDestination()));

        return AlertResponseDto.from(alert);
    }

    @Transactional
    public AlertResponseDto updateConfirmed(Long id, AlertConfirmedUpdateRequestDto dto, String actorEmail) {
        AlertEntity alert = findAlert(id);
        assertAlertAccess(alert, requireManager(actorEmail));
        alert.setConfirmed(dto.confirmed());

        return AlertResponseDto.from(alert);
    }

    /**
     * OPEN -> DONE/NOT_DONE registra quando o alerta foi respondido (answeredAt);
     * DONE marca confirmed. Voltar para OPEN limpa os dois.
     */
    @Transactional
    public AlertResponseDto updateStatus(Long id, AlertStatusUpdateRequestDto dto, String actorEmail) {
        AlertEntity alert = findAlert(id);
        assertAlertAccess(alert, requireManager(actorEmail));
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
    public void deleteAlert(Long id, String actorEmail) {
        AlertEntity alert = findAlert(id);
        assertAlertAccess(alert, requireManager(actorEmail));
        alertRepository.delete(alert);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AlertResponseDto> findAlerts(
            Long requestedStoreId, AlertStatus status, Pageable pageable, String actorEmail) {
        ManagerEntity actor = requireManager(actorEmail);
        Long storeId = actor.getStore().getId();
        if (requestedStoreId != null && !requestedStoreId.equals(storeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode consultar alertas da sua unidade");
        }
        AlertDestination destination = destinationFor(actor);
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

        Page<AlertEntity> result = status == null
                ? alertRepository.findByStoreIdAndDestination(storeId, destination, page)
                : alertRepository.findByStoreIdAndDestinationAndStatus(storeId, destination, status, page);
        return PageResponseDto.from(result, AlertResponseDto::from);
    }

    @Transactional(readOnly = true)
    public AlertResponseDto findAlertById(Long id, String actorEmail) {
        AlertEntity alert = findAlert(id);
        assertAlertAccess(alert, requireManager(actorEmail));
        return AlertResponseDto.from(alert);
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
    private ManagerEntity findManagerOfStore(Long managerId, Long storeId, AlertDestination destination) {
        ManagerEntity manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gerente não encontrado"));

        if (!storeId.equals(manager.getStore().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O gerente não pertence à loja do alerta");
        }
        String position = manager.getPosition().trim();
        boolean eligible = destination == AlertDestination.HR
                ? position.equalsIgnoreCase("RH") || position.equalsIgnoreCase("HR")
                : position.equalsIgnoreCase("MANAGER") || position.equalsIgnoreCase("GERENTE");
        if (!eligible) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O responsável não possui o papel exigido pelo destino do alerta");
        }
        return manager;
    }

    private ManagerEntity requireManager(String email) {
        return managerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Somente gestores podem acessar alertas"));
    }

    private static AlertDestination destinationFor(ManagerEntity manager) {
        String position = manager.getPosition().trim();
        if (position.equalsIgnoreCase("RH") || position.equalsIgnoreCase("HR")) {
            return AlertDestination.HR;
        }
        if (position.equalsIgnoreCase("MANAGER") || position.equalsIgnoreCase("GERENTE")) {
            return AlertDestination.MANAGER;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seu papel não possui acesso a alertas");
    }

    private static void assertDestination(ManagerEntity manager, AlertDestination destination) {
        if (destinationFor(manager) != destination) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Você não pode criar alertas para este destino");
        }
    }

    private static void assertAlertAccess(AlertEntity alert, ManagerEntity manager) {
        if (!alert.getStore().getId().equals(manager.getStore().getId())
                || destinationFor(manager) != alert.getDestination()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode acessar este alerta");
        }
    }

    private EmployeeEntity findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }
}