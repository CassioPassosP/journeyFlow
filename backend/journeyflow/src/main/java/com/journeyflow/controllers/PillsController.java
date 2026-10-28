package com.journeyflow.controllers;

import com.journeyflow.services.PillsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pills")
@RequiredArgsConstructor
public class PillsController {

    private final PillsService pillsService;

}
