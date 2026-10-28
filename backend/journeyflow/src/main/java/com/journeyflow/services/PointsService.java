package com.journeyflow.services;

import com.journeyflow.repositories.PointsRepository;
import org.springframework.stereotype.Service;

@Service
public class PointsService {

    private final PointsRepository pointsRepository;

    public PointsService(PointsRepository pointsRepository) {
        this.pointsRepository = pointsRepository;
    }
}
