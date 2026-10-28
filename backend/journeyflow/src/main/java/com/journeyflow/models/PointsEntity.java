package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "points_log")
public class PointsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long user_id;

    @Column(name = "reason")
    private String reason;

    @Column(name = "points")
    private Integer points;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
