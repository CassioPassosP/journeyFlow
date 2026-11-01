package com.journeyflow.services;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.journeyflow.dto.Auth.LoginRequestDto;
import com.journeyflow.dto.Auth.LoginResponseDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.ManagerRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    static final String ROLE_EMPLOYEE = "EMPLOYEE";
    static final String ROLE_MANAGER = "MANAGER";
    private static final String TOKEN_TYPE = "Bearer";

    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Hash descartável: quando o e-mail não existe ainda comparamos a senha com ele, para que
     * "e-mail inexistente" e "senha errada" levem o mesmo tempo (evita enumeração de usuários por timing).
     */
    private final String dummyHash;

    public AuthService(
            EmployeeRepository employeeRepository,
            ManagerRepository managerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.employeeRepository = employeeRepository;
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
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

    private static BadCredentialsException invalidCredentials() {
        return new BadCredentialsException("E-mail ou senha inválidos");
    }
}