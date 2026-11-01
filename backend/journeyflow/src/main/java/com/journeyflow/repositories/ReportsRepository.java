package com.journeyflow.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.journeyflow.models.ReportsEntity;
import com.journeyflow.models.enums.ReportStatus;

public interface ReportsRepository extends JpaRepository<ReportsEntity, Long> {

    Page<ReportsEntity> findByEmployeeId(UUID employeeId, Pageable pageable);

    Page<ReportsEntity> findByStatus(ReportStatus status, Pageable pageable);
}