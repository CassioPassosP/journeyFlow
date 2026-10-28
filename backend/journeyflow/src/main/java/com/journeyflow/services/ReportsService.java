package com.journeyflow.services;

import com.journeyflow.repositories.ReportsRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportsService {

    private final ReportsRepository reportsRepository;

    public ReportsService(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }
}
