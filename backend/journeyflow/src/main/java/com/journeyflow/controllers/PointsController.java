package com.journeyflow.controllers;

import com.journeyflow.services.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

}
