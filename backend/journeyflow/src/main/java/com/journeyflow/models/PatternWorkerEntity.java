package com.journeyflow.models;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class PatternWorkerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    long store_id;

    @Column(name = "nome", nullable = false)
    String name;

    @Column(name = "cargo", nullable = false)
    String position;

    @Column(nullable = false)
    String whatsapp;
}
