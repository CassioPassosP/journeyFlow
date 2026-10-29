package com.journeyflow.repositories;

import com.journeyflow.models.PillProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PillProgressRepository extends JpaRepository<PillProgressEntity, Long> {
}
