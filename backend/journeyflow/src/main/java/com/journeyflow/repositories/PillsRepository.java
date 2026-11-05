package com.journeyflow.repositories;

import com.journeyflow.models.PillsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface PillsRepository extends JpaRepository<PillsEntity, Long> {
    List<PillsEntity> findAll(Sort sort);
}
