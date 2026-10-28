package com.journeyflow.services;

import com.journeyflow.repositories.PillProgressRepository;
import org.springframework.stereotype.Service;

@Service
public class PillProgressService {

    private final PillProgressRepository pillProgressEntity;

    public PillProgressService(PillProgressRepository pillProgressEntity) {
        this.pillProgressEntity = pillProgressEntity;
    }
}
