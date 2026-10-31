package com.journeyflow.services;

import com.journeyflow.dto.Employee.EmployeeCreateRequestDto;
import com.journeyflow.dto.Employee.EmployeeResponseDto;
import com.journeyflow.dto.Employee.EmployeeUpdateRequestDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateRequestDto dto) {
        if (dto.store() == null || dto.store().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID da loja e obrigatorio");
        }
        if (dto.name() == null || dto.name().isBlank() || dto.email() == null || dto.email().isBlank()
                || dto.position() == null || dto.position().isBlank() || dto.password() == null || dto.password().isBlank()
                || dto.initiationDate() == null || dto.currentState() == null || dto.currentState().isBlank()
                || dto.startDate() == null || dto.lastActive() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campos obrigatorios do funcionario estao ausentes");
        }
        StoreEntity store = storeRepository.findById(dto.store().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loja nao encontrada"));

        EmployeeEntity employee = new EmployeeEntity();
        employee.setStore(store);
        employee.setName(dto.name());
        employee.setEmail(dto.email());
        employee.setPassword(passwordEncoder.encode(dto.password()));
        employee.setPosition(dto.position());
        employee.setInitiationDate(dto.initiationDate());
        employee.setCurrentState(dto.currentState());
        employee.setInLearningPath(dto.inLearningPath());
        employee.setLastInteraction(dto.lastInteraction());
        employee.setRegisterDate(dto.registerDate());
        employee.setStartDate(dto.startDate());
        employee.setConsent(dto.consent());
        employee.setConsentAt(dto.consentAt());
        employee.setStreak(dto.streak());
        employee.setLastActive(dto.lastActive());
        return new EmployeeResponseDto(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponseDto updateEmployee(UUID id, @Valid EmployeeUpdateRequestDto dto) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Funcionario nao encontrado"));
        if (dto.store() == null || dto.store().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID da loja e obrigatorio");
        }
        if (dto.name() == null || dto.name().isBlank() || dto.email() == null || dto.email().isBlank()
                || dto.position() == null || dto.position().isBlank() || dto.password() == null || dto.password().isBlank()
                || dto.initiationDate() == null || dto.currentState() == null || dto.currentState().isBlank()
                || dto.startDate() == null || dto.lastActive() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campos obrigatorios do funcionario estao ausentes");
        }
        StoreEntity store = storeRepository.findById(dto.store().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loja nao encontrada"));

        employee.setStore(store);
        employee.setName(dto.name());
        employee.setEmail(dto.email());
        employee.setPassword(passwordEncoder.encode(dto.password()));
        employee.setPosition(dto.position());
        employee.setInitiationDate(dto.initiationDate());
        employee.setCurrentState(dto.currentState());
        employee.setInLearningPath(dto.inLearningPath());
        employee.setLastInteraction(dto.lastInteraction());
        employee.setRegisterDate(dto.registerDate());
        employee.setStartDate(dto.startDate());
        employee.setConsent(dto.consent());
        employee.setConsentAt(dto.consentAt());
        employee.setStreak(dto.streak());
        employee.setLastActive(dto.lastActive());
        return new EmployeeResponseDto(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Funcionario nao encontrado"));
        employeeRepository.delete(employee);
    }

    @Transactional
    public List<EmployeeResponseDto> findAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeResponseDto::new)
                .toList();
    }

    @Transactional
    public EmployeeResponseDto findEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .map(EmployeeResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                                "Funcionario nao encontrado"));
    }
}