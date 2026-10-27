package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "pill_progress")
public class PillProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id", nullable = false)
    private EmployeeEntity employee;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id", nullable = false)
    private PillsEntity pill;

    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;
}
