package com.journeyflow.services;

import com.journeyflow.dto.Alert.AlertCreateRequestDto;
import com.journeyflow.dto.Alert.AlertResponseDto;
import com.journeyflow.dto.Alert.AlertUpdateRequestDto;
import com.journeyflow.models.AlertEntity;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.models.ReportsEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertStatus;
import com.journeyflow.models.enums.AlertUrgency;
import com.journeyflow.repositories.AlertRepository;
import com.journeyflow.repositories.ManagerRepository;
import com.journeyflow.repositories.ReportsRepository;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final ReportsRepository reportsRepository;
    private final ManagerRepository managerRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public AlertResponseDto createAlert(AlertCreateRequestDto dto) {
        AlertEntity alert = new AlertEntity();
        applyFields(alert, dto.report_id(), dto.assigned_to(), dto.store_id(), dto.destination(), dto.suggestion(),
                dto.urgency(), dto.status(), dto.reminder_sent(), dto.answered_at(), dto.alert_date(),
                dto.alert_title(), dto.confirmed(), dto.subtitle());
        return new AlertResponseDto(alertRepository.save(alert));
    }

    @Transactional
    public AlertResponseDto updateAlert(Long id, AlertUpdateRequestDto dto) {
        AlertEntity alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado"));
        applyFields(alert, dto.report_id(), dto.assigned_to(), dto.store_id(), dto.destination(), dto.suggestion(),
                dto.urgency(), dto.status(), dto.reminder_sent(), dto.answered_at(), dto.alert_date(),
                dto.alert_title(), dto.confirmed(), dto.subtitle());
        return new AlertResponseDto(alertRepository.save(alert));
    }

    @Transactional
    public void deleteAlert(Long id) {
        AlertEntity alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado"));
        alertRepository.delete(alert);
    }

    @Transactional
    public List<AlertResponseDto> findAllAlerts() {
        return alertRepository.findAll().stream().map(AlertResponseDto::new).toList();
    }

    @Transactional
    public AlertResponseDto findAlertById(Long id) {
        return alertRepository.findById(id)
                .map(AlertResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado"));
    }

    // Valida os dados do DTO, busca as entidades relacionadas e preenche o alerta.
    private void applyFields(AlertEntity alert, Long reportId, Long managerId, Long storeId,
                             AlertDestination destination, String suggestion, AlertUrgency urgency,
                             AlertStatus status, boolean reminderSent, OffsetDateTime answeredAt,
                             Date alertDate, String alertTitle, boolean confirmed, String subtitle) {
        if (reportId == null || storeId == null || destination == null || urgency == null || alertDate == null
                || alertTitle == null || subtitle == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos obrigatorios do alerta estao ausentes");
        }
        ReportsEntity report = reportsRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatorio nao encontrado"));
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja nao encontrada"));
        ManagerEntity manager = managerId == null ? null : managerRepository.findById(managerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gerente nao encontrado"));

        alert.setReport(report);
        alert.setAssignedTo(manager);
        alert.setStore(store);
        alert.setDestination(destination);
        alert.setSuggestion(suggestion);
        alert.setUrgency(urgency);
        if (status != null) alert.setStatus(status);
        alert.setReminderSent(reminderSent);
        alert.setAnsweredAt(answeredAt);
        alert.setAlertDate(alertDate.toLocalDate().atStartOfDay());
        alert.setAlertTitle(alertTitle);
        alert.setConfirmed(confirmed);
        alert.setSubtitle(subtitle);
    }
}