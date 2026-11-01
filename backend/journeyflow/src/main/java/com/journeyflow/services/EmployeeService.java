package com.journeyflow.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.dto.Employee.EmployeeCreateRequestDto;
import com.journeyflow.dto.Employee.EmployeeResponseDto;
import com.journeyflow.dto.Employee.EmployeeUpdateRequestDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final Sort DEFAULT_SORT = Sort.by("name");

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkerEmailValidator emailValidator;
    private final Clock clock;

    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateRequestDto dto) {
        String email = WorkerEmailValidator.normalize(dto.email());
        emailValidator.assertAvailable(email, null, null);
        StoreEntity store = findStore(dto.storeId());

        OffsetDateTime now = OffsetDateTime.now(clock);
        boolean consent = Boolean.TRUE.equals(dto.consent());

        EmployeeEntity employee = new EmployeeEntity();
        employee.setStore(store);
        employee.setName(dto.name().trim());
        employee.setEmail(email);
        employee.setPassword(passwordEncoder.encode(dto.password()));
        employee.setPosition(dto.position().trim());
        employee.setStartDate(dto.startDate());
        employee.setCurrentState(dto.currentState().trim());
        employee.setInLearningPath(Boolean.TRUE.equals(dto.inLearningPath()));

        // Campos controlados pelo sistema (não vêm do cliente):
        employee.setRegisterDate(now);
        employee.setConsent(consent);
        employee.setConsentAt(consent ? now : null);
        employee.setStreak(0);
        employee.setLastActive(dto.startDate()); // ainda sem atividade; o streak começa na 1ª interação

        // saveAndFlush: o id é UUID (INSERT adiado até o flush); garante createdAt preenchido no retorno
        return EmployeeResponseDto.from(employeeRepository.saveAndFlush(employee));
    }

    //Atualiza só os dados cadastrais.
    @Transactional
    public EmployeeResponseDto updateEmployee(UUID id, EmployeeUpdateRequestDto dto) {
        EmployeeEntity employee = findEmployee(id);

        String email = WorkerEmailValidator.normalize(dto.email());
        emailValidator.assertAvailable(email, id, null);
        StoreEntity store = findStore(dto.storeId());

        employee.setStore(store);
        employee.setName(dto.name().trim());
        employee.setEmail(email);
        employee.setPosition(dto.position().trim());
        employee.setStartDate(dto.startDate());
        employee.setCurrentState(dto.currentState().trim());
        employee.setInLearningPath(Boolean.TRUE.equals(dto.inLearningPath()));

        return EmployeeResponseDto.from(employee);
    }

    /** Concede ou revoga o consentimento (LGPD). consentAt guarda quando foi concedido. */
    @Transactional
    public EmployeeResponseDto updateConsent(UUID id, boolean consent) {
        EmployeeEntity employee = findEmployee(id);

        if (employee.isConsent() != consent) {
            employee.setConsent(consent);
            employee.setConsentAt(consent ? OffsetDateTime.now(clock) : null);
        }
        return EmployeeResponseDto.from(employee);
    }

    /**
     * Registra uma interação do colaborador hoje e atualiza a sequência (streak):
     *  - já contou hoje            -> mantém;
     *  - última atividade = ontem  -> +1;
     *  - qualquer outro caso       -> recomeça em 1.
     */
    @Transactional
    public EmployeeResponseDto registerActivity(UUID id) {
        EmployeeEntity employee = findEmployee(id);

        LocalDate today = LocalDate.now(clock);
        LocalDate lastActive = employee.getLastActive();
        boolean hasStreak = employee.getStreak() > 0;

        boolean alreadyCountedToday = hasStreak && today.equals(lastActive);
        boolean continuesStreak = hasStreak && today.minusDays(1).equals(lastActive);

        if (continuesStreak) {
            employee.setStreak(employee.getStreak() + 1);
        } else if (!alreadyCountedToday) {
            employee.setStreak(1);
        }

        employee.setLastActive(today);
        employee.setLastInteraction(OffsetDateTime.now(clock));

        return EmployeeResponseDto.from(employee);
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        EmployeeEntity employee = findEmployee(id);
        employeeRepository.delete(employee);
        employeeRepository.flush(); // FK violada (mensagens, pontos, relatos...) vira 409
    }

    @Transactional(readOnly = true)
    public PageResponseDto<EmployeeResponseDto> findEmployees(Long storeId, Pageable pageable) {
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

        Page<EmployeeEntity> result = storeId == null
                ? employeeRepository.findAll(page)
                : employeeRepository.findByStoreId(storeId, page);

        return PageResponseDto.from(result, EmployeeResponseDto::from);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDto findEmployeeById(UUID id) {
        return EmployeeResponseDto.from(findEmployee(id));
    }

    private EmployeeEntity findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    private StoreEntity findStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja não encontrada"));
    }
}