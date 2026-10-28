package com.journeyflow.models;

import java.time.OffsetDateTime;
import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "alerts")
public class AlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private Long report_id;

    @Column(name = "assigned_to")
    private Long assigned_to;

    @Column(name = "destination")
    private String destination;

    @Column(name = "suggestion")
    private String suggestion;

    @Column(name = "urgency")
    private String urgency;

    @Column(name = "status")
    private String status;

    @Column(name = "reminder_sent")
    private boolean reminder_sent;

    @Column(name = "created_at")
    private OffsetDateTime created_at;

    @Column(name = "answered_at")
    private OffsetDateTime answered_at;

    @Column(name = "alert_date", nullable = false)
    private Date alert_date;

    @Column(name = "alert_title", nullable = false)
    private String alert_title;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Column(name = "subtitle", nullable = false)
    private String subtitle;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employeeEntity;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

}
