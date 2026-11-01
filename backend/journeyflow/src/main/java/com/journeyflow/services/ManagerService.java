package com.journeyflow.services;

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
import com.journeyflow.dto.Manager.ManagerCreateRequestDto;
import com.journeyflow.dto.Manager.ManagerResponseDto;
import com.journeyflow.dto.Manager.ManagerUpdateRequestDto;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.repositories.ManagerRepository;
import com.journeyflow.repositories.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {

    /** Ordenação fixa: não aceitamos ?sort= do cliente (poderia ordenar pelo hash da senha). */
    private static final Sort DEFAULT_SORT = Sort.by("name");

    private final ManagerRepository managerRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkerEmailValidator emailValidator;

    @Transactional
    public ManagerResponseDto createManager(ManagerCreateRequestDto dto) {
        String email = WorkerEmailValidator.normalize(dto.email());
        emailValidator.assertAvailable(email, null, null);
        StoreEntity store = findStore(dto.storeId());

        ManagerEntity manager = new ManagerEntity();
        manager.setStore(store);
        manager.setName(dto.name().trim());
        manager.setEmail(email);
        manager.setPassword(passwordEncoder.encode(dto.password()));
        manager.setPosition(dto.position().trim());

        return ManagerResponseDto.from(managerRepository.save(manager));
    }

    /** A senha não é alterada aqui: o DTO de update não a recebe (antes ela era re-hasheada a cada PUT). */
    @Transactional
    public ManagerResponseDto updateManager(Long id, ManagerUpdateRequestDto dto) {
        ManagerEntity manager = findManager(id);

        String email = WorkerEmailValidator.normalize(dto.email());
        emailValidator.assertAvailable(email, null, id);
        StoreEntity store = findStore(dto.storeId());

        manager.setStore(store);
        manager.setName(dto.name().trim());
        manager.setEmail(email);
        manager.setPosition(dto.position().trim());

        return ManagerResponseDto.from(manager);
    }

    @Transactional
    public void deleteManager(Long id) {
        ManagerEntity manager = findManager(id);
        managerRepository.delete(manager);
        managerRepository.flush(); // FK violada (gerente com alertas atribuídos) vira 409
    }

    @Transactional(readOnly = true)
    public PageResponseDto<ManagerResponseDto> findManagers(Long storeId, Pageable pageable) {
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);

        Page<ManagerEntity> result = storeId == null
                ? managerRepository.findAll(page)
                : managerRepository.findByStoreId(storeId, page);

        return PageResponseDto.from(result, ManagerResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ManagerResponseDto findManagerById(Long id) {
        return ManagerResponseDto.from(findManager(id));
    }

    private ManagerEntity findManager(Long id) {
        return managerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gerente não encontrado"));
    }

    private StoreEntity findStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja não encontrada"));
    }
}