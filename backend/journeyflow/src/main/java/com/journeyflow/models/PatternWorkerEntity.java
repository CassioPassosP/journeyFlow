package com.journeyflow.models;

import jakarta.persistence.MappedSuperclass;

import java.util.Date;

@MappedSuperclass
public abstract class PatternWorkerEntity {
    long id;
    long store_id;
    String name;
    String position;
    String whatsapp;
}
