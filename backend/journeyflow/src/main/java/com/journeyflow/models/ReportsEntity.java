package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "reports")
public class ReportsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "original_text", nullable = false)
    private String originalText;

    @Column(name = "edited_text", nullable = false)
    private String editedText;

    @Column(name = "summary")
    private String summary;

    @Column(name = "topic")
    private String topic;

    @Column(name = "kind")
    private String kind;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "help_request", nullable = false)
    private boolean helpRequest;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
