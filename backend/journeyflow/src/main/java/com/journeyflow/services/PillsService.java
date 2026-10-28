package com.journeyflow.services;

import com.journeyflow.repositories.PillsRepository;
import org.springframework.stereotype.Service;

@Service
public class PillsService {

    private final PillsRepository pillsRepository;

    public PillsService(PillsRepository pillsRepository) {
        this.pillsRepository = pillsRepository;
    }
}
