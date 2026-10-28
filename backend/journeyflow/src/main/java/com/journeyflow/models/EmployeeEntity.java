package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
public class EmployeeEntity extends PatternWorkerEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id", nullable = false)
    long id;

    @Column(name = "data_inicio_contrato", nullable = false)
    Date initiationDate;

    @Column(name = "estado_atual", nullable = false)
    String currentState;

    @Column(name = "em_trilha_aprendizagem", nullable = false)
    boolean inLearningPath;

    @Column(name = "ultima_interacao")
    OffsetDateTime lastInteraction;

    @Column(name = "data_cadastro")
    OffsetDateTime registerDate;

    @Column(name = "start_date", nullable = false)
    Date startDate;

    @Column(name = "consent")
    boolean consent;

    @Column(name = "consent_at")
    private OffsetDateTime consentAt;

    @Column(name = "points")
    private int points;

    @Column(name = "streak")
    private int streak;

    @Column(name = "last_active", nullable = false)
    Date lastActive;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
