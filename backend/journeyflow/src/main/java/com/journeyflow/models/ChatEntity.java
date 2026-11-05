package com.journeyflow.models;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "messages")
public class ChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false)
    private MessageSender sender;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private ContentSource source = ContentSource.TEXT;

    @CreationTimestamp
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

    // Preenchido apenas em mensagens que apresentam ou respondem a um quiz de pílula.
    @Column(name = "pill_id")
    private Long pillId;
}
