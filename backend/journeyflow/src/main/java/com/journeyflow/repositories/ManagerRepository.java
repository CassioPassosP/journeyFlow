package com.journeyflow.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.journeyflow.models.ManagerEntity;

public interface ManagerRepository extends JpaRepository<ManagerEntity, Long> {

    Optional<ManagerEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Page<ManagerEntity> findByStoreId(Long storeId, Pageable pageable);
}