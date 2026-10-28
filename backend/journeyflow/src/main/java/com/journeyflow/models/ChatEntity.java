package com.journeyflow.models;

import java.time.Instant;
import java.time.OffsetDateTime;

import jakarta.persistence.*;
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
    private EmployeeEntity employee;

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

    //spring n ta deixando eu rodar o projeto com esse atributo
//    @JdbcTypeCode(SqlTypes.VECTOR)
//    @Array(length = 1536)
//    private float[] embedding;

    @Column(name = "date_sent", nullable = false)
    private Instant dateSent;
}
