package com.journeyflow.models;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "employee")
public class EmployeeEntity extends PatternWorkerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_inicio_contrato", nullable = false)
    private LocalDate initiationDate;

    @Column(name = "current_state", nullable = false)
    private String currentState;

    @Column(name = "in_learning_path", nullable = false)
    private boolean inLearningPath;

    @Column(name = "last_interaction")
    private OffsetDateTime lastInteraction;

    @Column(name = "register_date")
    private OffsetDateTime registerDate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "consent")
    private boolean consent;

    @Column(name = "consent_at")
    private OffsetDateTime consentAt;

    @Column(name = "streak")
    private int streak;

    @Column(name = "last_active", nullable = false)
    private LocalDate lastActive;
}

/*
 * MELHORIAS:
 *  - @GeneratedValue(IDENTITY) não funciona com UUID (IDENTITY é para colunas auto-incrementais).
 *    Agora GenerationType.UUID (JPA 3.1).
 *  - java.util.Date -> LocalDate nas colunas DATE (a lógica de streak/liberação de pílulas depende disso).
 *  - @Data + @EqualsAndHashCode(callSuper = true) removidos: o pai não implementa equals/hashCode e
 *    @Data em entidade com associação LAZY pode disparar carregamento ao chamar toString/hashCode.
 *  - "current_State" -> "current_state".
 */

/**
 * employee.points_id / messages_id / pills_id existem no banco com a FK invertida
 * (colaborador -> 1 registro filho). O lado "muitos" correto fica nas tabelas filhas
 * (points_log, messages, pill_progress), que apontam para o colaborador via @ManyToOne.
 * Aquelas tres colunas deixam de ser mapeadas.
 */