package com.journeyflow.models;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class PatternWorkerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "nome", nullable = false)
    String name;

    @Column(name = "cargo", nullable = false)
    String position;

    @Column(nullable = false)
    String whatsapp;
}
