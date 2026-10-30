package com.journeyflow.services;

import com.journeyflow.dto.Alert.AlertCreateRequestDto;
import com.journeyflow.dto.Alert.AlertResponseDto;
import com.journeyflow.dto.Alert.AlertUpdateRequestDto;
import com.journeyflow.models.AlertEntity;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.models.ReportsEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.AlertRepository;
import com.journeyflow.repositories.ManagerRepository;
import com.journeyflow.repositories.ReportsRepository;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        if (dto.report_id() == null || dto.store_id() == null || dto.destination() == null || dto.urgency() == null
                || dto.alert_date() == null || dto.alert_title() == null || dto.alert_title().isBlank()
                || dto.subtitle() == null || dto.subtitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campos obrigatorios do alerta estao ausentes");
        }
        ReportsEntity report = reportsRepository.findById(dto.report_id())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Relatorio nao encontrado"));

        StoreEntity store = storeRepository.findById(dto.store_id())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loja nao encontrada"));

        ManagerEntity manager = dto.assigned_to() == null ? null : managerRepository.findById(dto.assigned_to())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Gerente nao encontrado"));

        AlertEntity alert = new AlertEntity();
        alert.setReport(report);
        alert.setAssignedTo(manager);
        alert.setStore(store);
        alert.setDestination(dto.destination());
        alert.setSuggestion(dto.suggestion());
        alert.setUrgency(dto.urgency());
        if (dto.status() != null) alert.setStatus(dto.status());
        alert.setReminderSent(dto.reminder_sent());
        alert.setAnsweredAt(dto.answered_at());
        alert.setAlertDate(dto.alert_date().toLocalDate().atStartOfDay());
        alert.setAlertTitle(dto.alert_title());
        alert.setConfirmed(dto.confirmed());
        alert.setSubtitle(dto.subtitle());

        return new AlertResponseDto(alertRepository.save(alert));
    }

    @Transactional
    public AlertResponseDto updateAlert(Long id, AlertUpdateRequestDto dto) {
        AlertEntity alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado"));
        if (dto.report_id() == null || dto.store_id() == null || dto.destination() == null || dto.urgency() == null
                || dto.alert_date() == null || dto.alert_title() == null || dto.alert_title().isBlank()
                || dto.subtitle() == null || dto.subtitle().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campos obrigatorios do alerta estao ausentes");
        }
        ReportsEntity report = reportsRepository.findById(dto.report_id())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Relatorio nao encontrado"));

        StoreEntity store = storeRepository.findById(dto.store_id())

                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loja nao encontrada"));

        ManagerEntity manager = dto.assigned_to() == null ? null : managerRepository.findById(dto.assigned_to())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Gerente nao encontrado"));

        alert.setReport(report);
        alert.setAssignedTo(manager);
        alert.setStore(store);
        alert.setDestination(dto.destination());
        alert.setSuggestion(dto.suggestion());
        alert.setUrgency(dto.urgency());
        if (dto.status() != null) alert.setStatus(dto.status());
        alert.setReminderSent(dto.reminder_sent());
        alert.setAnsweredAt(dto.answered_at());
        alert.setAlertDate(dto.alert_date().toLocalDate().atStartOfDay());
        alert.setAlertTitle(dto.alert_title());
        alert.setConfirmed(dto.confirmed());
        alert.setSubtitle(dto.subtitle());

        return new AlertResponseDto(alertRepository.save(alert));
    }

    @Transactional
    public void deleteAlert(Long id) {
        AlertEntity alert = alertRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Alerta nao encontrado"));

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
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Alerta nao encontrado"));
    }
}