package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Antes: @Id Long id + dois @OneToOne(cascade = ALL) ambos com @JoinColumn(name = "id")
 * (colunas duplicadas e cascade que apagaria colaborador/pilula junto com o progresso).
 * Agora: chave composta (employee_id, pill_id) + dois @ManyToOne LAZY sem cascade.
 * A coluna identity "id" da tabela e preenchida pelo banco e nao e mapeada.
 */
@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "pill_progress")
public class PillProgressEntity {

    @EmbeddedId
    private PillProgressId id;

    @MapsId("employeeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @MapsId("pillId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pill_id", nullable = false)
    private PillsEntity pill;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private OffsetDateTime completedAt;
}