package com.journeyflow.repositories;

import com.journeyflow.models.ReportsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportsRepository extends JpaRepository<ReportsEntity, Long> {
}
