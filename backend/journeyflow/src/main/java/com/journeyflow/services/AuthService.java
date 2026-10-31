package com.journeyflow.services;

import com.journeyflow.dto.Auth.LoginRequestDto;
import com.journeyflow.dto.Auth.LoginResponseDto;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.ManagerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDto login(LoginRequestDto dto) {

        var employee =
                employeeRepository.findByEmail(dto.email());

        var manager =
                managerRepository.findByEmail(dto.email());

        if (employee.isPresent() && manager.isPresent()) {
            throw new IllegalStateException(
                    "Email duplicado entre funcionários e gerentes"
            );
        }

        if (employee.isPresent()) {

            EmployeeEntity user = employee.get();

            validatePassword(dto.password(), user.getPassword());

            return createResponse(
                    user.getId().toString(),
                    user.getEmail(),
                    "EMPLOYEE"
            );
        }

        if (manager.isPresent()) {

            ManagerEntity user = manager.get();

            validatePassword(dto.password(), user.getPassword());

            return createResponse(
                    user.getId().toString(),
                    user.getEmail(),
                    "MANAGER"
            );
        }

        throw new BadCredentialsException(
                "Email ou senha inválidos"
        );
    }

    private void validatePassword(
            String rawPassword,
            String encodedPassword) {

        if (!passwordEncoder.matches(
                rawPassword,
                encodedPassword)) {

            throw new BadCredentialsException(
                    "Email ou senha inválidos"
            );
        }
    }

    private LoginResponseDto createResponse(
            String id,
            String email,
            String role) {

        String token = jwtService.generateToken(
                id,
                email,
                role
        );

        return new LoginResponseDto(
                token,
                "Bearer",
                id,
                email,
                role
        );
    }
}