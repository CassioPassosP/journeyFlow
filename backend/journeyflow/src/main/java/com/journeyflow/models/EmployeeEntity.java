package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "employee")
public class EmployeeEntity extends PatternWorkerEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_id")
    private PointsEntity points;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "messages_id")
    private ChatEntity messages;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "pills_id")
    private PillsEntity pills;

    @Column(name = "data_inicio_contrato", nullable = false)
    private Date initiationDate;

    @Column(name = "current_State", nullable = false)
    private String currentState;

    @Column(name = "in_learning_path", nullable = false)
    private boolean inLearningPath;

    @Column(name = "last_interaction")
    private OffsetDateTime lastInteraction;

    @Column(name = "register_date")
    private OffsetDateTime registerDate;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "consent")
    private boolean consent;

    @Column(name = "consent_at")
    private OffsetDateTime consentAt;

    @Column(name = "streak")
    private int streak;

    @Column(name = "last_active", nullable = false)
    private Date lastActive;
}
