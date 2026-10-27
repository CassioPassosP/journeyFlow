package com.journeyflow.models;

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

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employeeEntity;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(nullable = false)
    private String alert_title;

    @Column(nullable = false)
    private String subtitle;

    @Column(nullable = false)
    private boolean confirmed;

    @Column(nullable = false)
    private Date alert_date;
}
