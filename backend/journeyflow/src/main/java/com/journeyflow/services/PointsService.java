package com.journeyflow.services;

import com.journeyflow.repositories.PointsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointsService {

    @Autowired
    private PointsRepository pointsRepository;

}
