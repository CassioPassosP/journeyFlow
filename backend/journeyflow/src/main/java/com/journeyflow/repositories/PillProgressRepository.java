package com.journeyflow.repositories;

import com.journeyflow.models.PillProgressEntity;
import com.journeyflow.models.PillProgressId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PillProgressRepository extends JpaRepository<PillProgressEntity, PillProgressId> {
}