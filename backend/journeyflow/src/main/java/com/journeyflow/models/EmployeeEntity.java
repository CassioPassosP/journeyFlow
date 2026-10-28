package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
public class EmployeeEntity extends PatternWorkerEntity{

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
}
