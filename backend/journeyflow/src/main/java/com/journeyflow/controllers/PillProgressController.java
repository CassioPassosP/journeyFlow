package com.journeyflow.controllers;

import com.journeyflow.services.PillProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pillprogress")
@RequiredArgsConstructor
public class PillProgressController {

    private final PillProgressService pillProgressService;

}
