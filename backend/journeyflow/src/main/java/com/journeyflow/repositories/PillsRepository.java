package com.journeyflow.repositories;

import com.journeyflow.models.PillsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PillsRepository extends JpaRepository<PillsEntity, Long> {
}
