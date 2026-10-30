package com.journeyflow.services;

import com.journeyflow.repositories.PillProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PillProgressService {

    @Autowired
    private PillProgressRepository pillProgressEntity;

}
