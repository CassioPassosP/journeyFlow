package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "manager")
public class ManagerEntity extends PatternWorkerEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manager_id", nullable = false)
    private UUID id;
}
