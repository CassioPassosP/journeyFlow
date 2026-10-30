package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "points_log")
public class PointsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private EmployeeEntity employee;
 
    @Column(name = "reason", nullable = false)
    private String reason;
 
    @Column(name = "points", nullable = false)
    private Integer points;
 
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
