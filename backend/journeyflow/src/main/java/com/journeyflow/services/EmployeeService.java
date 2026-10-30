package com.journeyflow.services;

import com.journeyflow.dto.Employee.EmployeeCreateRequestDto;
import com.journeyflow.dto.Employee.EmployeeResponseDto;
import com.journeyflow.dto.Employee.EmployeeUpdateRequestDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Date;
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
        EmployeeEntity employee = new EmployeeEntity();
        applyFields(employee, dto.store(), dto.name(), dto.email(), dto.password(), dto.position(),
                dto.initiationDate(), dto.currentState(), dto.inLearningPath(), dto.lastInteraction(),
                dto.registerDate(), dto.startDate(), dto.consent(), dto.consentAt(), dto.streak(), dto.lastActive());
        return new EmployeeResponseDto(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponseDto updateEmployee(UUID id, EmployeeUpdateRequestDto dto) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario nao encontrado"));
        applyFields(employee, dto.store(), dto.name(), dto.email(), dto.password(), dto.position(),
                dto.initiationDate(), dto.currentState(), dto.inLearningPath(), dto.lastInteraction(),
                dto.registerDate(), dto.startDate(), dto.consent(), dto.consentAt(), dto.streak(), dto.lastActive());
        return new EmployeeResponseDto(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario nao encontrado"));
        employeeRepository.delete(employee);
    }

    @Transactional
    public List<EmployeeResponseDto> findAllEmployees() {
        return employeeRepository.findAll().stream().map(EmployeeResponseDto::new).toList();
    }

    @Transactional
    public EmployeeResponseDto findEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .map(EmployeeResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario nao encontrado"));
    }

    // Valida os dados, busca a loja e preenche os campos do funcionario.
    private void applyFields(EmployeeEntity employee, StoreEntity storeDto, String name, String email, String password,
                             String position, Date initiationDate, String currentState, boolean inLearningPath,
                             OffsetDateTime lastInteraction, OffsetDateTime registerDate, Date startDate,
                             boolean consent, OffsetDateTime consentAt, int streak, Date lastActive) {
        if (storeDto == null || storeDto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da loja e obrigatorio");
        }
        if (name == null || name.isBlank() || email == null || email.isBlank() || position == null || position.isBlank()
                || initiationDate == null || currentState == null || currentState.isBlank() || startDate == null || lastActive == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos obrigatorios do funcionario estao ausentes");
        }
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha obrigatoria");
        }
        StoreEntity store = storeRepository.findById(storeDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja nao encontrada"));
        employee.setStore(store);
        employee.setName(name);
        employee.setEmail(email);
        employee.setPassword(passwordEncoder.encode(password));
        employee.setPosition(position);
        employee.setInitiationDate(initiationDate);
        employee.setCurrentState(currentState);
        employee.setInLearningPath(inLearningPath);
        employee.setLastInteraction(lastInteraction);
        employee.setRegisterDate(registerDate);
        employee.setStartDate(startDate);
        employee.setConsent(consent);
        employee.setConsentAt(consentAt);
        employee.setStreak(streak);
        employee.setLastActive(lastActive);
    }
}