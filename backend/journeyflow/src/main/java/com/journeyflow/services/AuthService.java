package com.journeyflow.services;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.journeyflow.dto.Auth.LoginRequestDto;
import com.journeyflow.dto.Auth.LoginResponseDto;
import com.journeyflow.dto.Auth.RegisterRequestDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.ManagerRepository;
import com.journeyflow.repositories.StoreRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    static final String ROLE_EMPLOYEE = "EMPLOYEE";
    static final String ROLE_MANAGER = "MANAGER";
    private static final String TOKEN_TYPE = "Bearer";

    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WorkerEmailValidator emailValidator;
    private final Clock clock;

    /**
     * Hash descartável: quando o e-mail não existe ainda comparamos a senha com ele, para que
     * "e-mail inexistente" e "senha errada" levem o mesmo tempo (evita enumeração de usuários por timing).
     */
    private final String dummyHash;

    public AuthService(
            EmployeeRepository employeeRepository,
            ManagerRepository managerRepository,
            StoreRepository storeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            WorkerEmailValidator emailValidator,
            Clock clock) {

        this.employeeRepository = employeeRepository;
        this.managerRepository = managerRepository;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailValidator = emailValidator;
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public LoginResponseDto register(RegisterRequestDto dto) {
        if (dto.role() == RegisterRequestDto.Role.EMPLOYEE) {
            validateEmployeeFields(dto);
        }

        String email = WorkerEmailValidator.normalize(dto.email());
        emailValidator.assertAvailable(email, null, null);
        StoreEntity store = storeRepository.findById(dto.storeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja não encontrada"));

        if (dto.role() == RegisterRequestDto.Role.MANAGER) {
            ManagerEntity manager = new ManagerEntity();
            manager.setStore(store);
            manager.setName(dto.name().trim());
            manager.setEmail(email);
            manager.setPassword(passwordEncoder.encode(dto.password()));
            manager.setPosition(dto.role().name());

            ManagerEntity saved = managerRepository.save(manager);
            return buildResponse(saved.getId().toString(), saved.getEmail(), ROLE_MANAGER);
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        boolean consent = Boolean.TRUE.equals(dto.consent());

        EmployeeEntity employee = new EmployeeEntity();
        employee.setStore(store);
        employee.setName(dto.name().trim());
        employee.setEmail(email);
        employee.setPassword(passwordEncoder.encode(dto.password()));
        employee.setPosition(dto.role().name());
        employee.setStartDate(dto.startDate());
        employee.setCurrentState(dto.currentState().trim());
        employee.setInLearningPath(Boolean.TRUE.equals(dto.inLearningPath()));
        employee.setRegisterDate(now);
        employee.setConsent(consent);
        employee.setConsentAt(consent ? now : null);
        employee.setStreak(0);
        employee.setLastActive(dto.startDate());

        EmployeeEntity saved = employeeRepository.saveAndFlush(employee);
        return buildResponse(saved.getId().toString(), saved.getEmail(), ROLE_EMPLOYEE);
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        String email = WorkerEmailValidator.normalize(dto.email());

        Optional<EmployeeEntity> employee = employeeRepository.findByEmailIgnoreCase(email);
        Optional<ManagerEntity> manager = managerRepository.findByEmailIgnoreCase(email);

        if (employee.isPresent() && manager.isPresent()) {
            // Dado legado inconsistente: registra para correção, mas não revela nada ao cliente.
            log.error("E-mail {} cadastrado ao mesmo tempo como colaborador e gerente", email);
            throw invalidCredentials();
        }

        if (employee.isPresent()) {
            EmployeeEntity user = employee.get();
            checkPassword(dto.password(), user.getPassword());
            return buildResponse(user.getId().toString(), user.getEmail(), ROLE_EMPLOYEE);
        }

        if (manager.isPresent()) {
            ManagerEntity user = manager.get();
            checkPassword(dto.password(), user.getPassword());
            return buildResponse(user.getId().toString(), user.getEmail(), ROLE_MANAGER);
        }

        passwordEncoder.matches(dto.password(), dummyHash);
        throw invalidCredentials();
    }

    private void checkPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw invalidCredentials();
        }
    }

    private LoginResponseDto buildResponse(String id, String email, String role) {
        String token = jwtService.generateToken(id, email, role);
        return new LoginResponseDto(token, TOKEN_TYPE, id, email, role);
    }

    private static void validateEmployeeFields(RegisterRequestDto dto) {
        if (dto.startDate() == null
                || dto.currentState() == null
                || dto.currentState().isBlank()
                || dto.inLearningPath() == null
                || dto.consent() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cadastro de funcionário exige startDate, currentState, inLearningPath e consent");
        }
    }

    private static BadCredentialsException invalidCredentials() {
        return new BadCredentialsException("E-mail ou senha inválidos");
    }
}