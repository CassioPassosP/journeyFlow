package com.journeyflow.repositories;

import com.journeyflow.models.ManagerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ManagerRepository extends JpaRepository<ManagerEntity, UUID> {
}
