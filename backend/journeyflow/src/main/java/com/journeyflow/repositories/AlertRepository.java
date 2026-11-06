package com.journeyflow.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.journeyflow.models.AlertEntity;
import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertStatus;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    Page<AlertEntity> findByStoreId(Long storeId, Pageable pageable);

    Page<AlertEntity> findByStatus(AlertStatus status, Pageable pageable);

    Page<AlertEntity> findByStoreIdAndStatus(Long storeId, AlertStatus status, Pageable pageable);

    Page<AlertEntity> findByStoreIdAndDestination(Long storeId, AlertDestination destination, Pageable pageable);

    Page<AlertEntity> findByStoreIdAndDestinationAndStatus(
            Long storeId, AlertDestination destination, AlertStatus status, Pageable pageable);
}