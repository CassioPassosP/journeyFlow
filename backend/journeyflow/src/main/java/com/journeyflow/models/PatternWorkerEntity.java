package com.journeyflow.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Date;

@MappedSuperclass
public abstract class PatternWorkerEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email",nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String position;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
