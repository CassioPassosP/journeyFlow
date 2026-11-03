package com.journeyflow.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.journeyflow.models.PillProgressEntity;
import com.journeyflow.models.PillProgressId; // CORREÇÃO: import ausente -> o projeto não compilava

public interface PillProgressRepository extends JpaRepository<PillProgressEntity, PillProgressId> {

    @Query("select pp from PillProgressEntity pp where pp.id.employeeId = :employeeId order by pp.completedAt desc")
    List<PillProgressEntity> listByEmployeeId(@Param("employeeId") UUID employeeId);
}