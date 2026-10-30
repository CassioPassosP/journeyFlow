package com.journeyflow.services;

import com.journeyflow.repositories.ReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportsService {

    @Autowired
    private  ReportsRepository reportsRepository;

}
