package com.journeyflow.controllers;

import com.journeyflow.services.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

}
