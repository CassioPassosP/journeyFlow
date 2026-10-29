package com.journeyflow.repositories;

import com.journeyflow.models.PointsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsRepository extends JpaRepository<PointsEntity, Long> {
}
