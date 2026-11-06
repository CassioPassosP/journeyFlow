package com.journeyflow.services;

import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertUrgency;

public record AnalysisResult(String kind, AlertDestination destination, AlertUrgency urgency) {}