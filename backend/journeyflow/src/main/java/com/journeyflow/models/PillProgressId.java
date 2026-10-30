package com.journeyflow.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/** Chave composta (user_id, pill_id) de pill_progress, igual a PRIMARY KEY do banco. */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PillProgressId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "pill_id")
    private Long pillId;
}