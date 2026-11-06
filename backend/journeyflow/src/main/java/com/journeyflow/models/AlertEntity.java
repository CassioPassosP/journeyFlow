package com.journeyflow.models;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertStatus;
import com.journeyflow.models.enums.AlertUrgency;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "alerts")
public class AlertEntity {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private ReportsEntity report;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private ManagerEntity assignedTo;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }
 
    @Enumerated(EnumType.STRING)
    @Column(name = "destination", nullable = false)
    private AlertDestination destination;
 
    @Column(name = "suggestion")
    private String suggestion;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false)
    private AlertUrgency urgency;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertStatus status = AlertStatus.OPEN;
 
    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent = false;
 
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
 
    @Column(name = "answered_at")
    private OffsetDateTime answeredAt;
 
    @Column(name = "alert_date", nullable = false)
    private LocalDateTime alertDate;
 
    @Column(name = "alert_title", nullable = false)
    private String alertTitle;
 
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed = false;
 
    @Column(name = "subtitle", nullable = false)
    private String subtitle;

}
