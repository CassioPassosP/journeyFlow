package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.ReportKind;
import com.journeyflow.models.enums.ReportStatus;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "reports")
public class ReportsEntity {
     
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
 
    @Column(name = "original_text", nullable = false)
    private String originalText;
 
    @Column(name = "edited_text", nullable = false)
    private String editedText;
 
    @Column(name = "summary")
    private String summary;
 
    @Column(name = "topic")
    private String topic;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "kind")
    private ReportKind kind;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private ContentSource source = ContentSource.TEXT;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.ANALYZED;
 
    @Column(name = "help_request", nullable = false)
    private boolean helpRequest = false;
 
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
