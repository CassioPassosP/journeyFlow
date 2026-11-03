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

/** Chave composta (employee_id, pill_id) de pill_progress, igual à PRIMARY KEY do banco. */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PillProgressId implements Serializable {

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "pill_id")
    private Long pillId;
}