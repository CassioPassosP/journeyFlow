package com.journeyflow.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.journeyflow.models.PointsEntity;

public interface PointsRepository extends JpaRepository<PointsEntity, Long> {

    Page<PointsEntity> findByEmployeeId(UUID employeeId, Pageable pageable);

    /** Saldo calculado no banco (SUM), em vez de carregar o histórico inteiro na memória. */
    @Query("select coalesce(sum(p.points), 0L) from PointsEntity p where p.employee.id = :employeeId")
    Long sumPointsByEmployeeId(@Param("employeeId") UUID employeeId);
}