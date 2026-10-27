package com.journeyflow.models;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "messages")
public class ChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employeeEntity;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sender", nullable = false)
    private String sender;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    @Column(name = "date_sent", nullable = false)
    private LocalDateTime dateSent;
}
