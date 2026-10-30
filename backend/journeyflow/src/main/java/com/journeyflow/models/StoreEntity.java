package com.journeyflow.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "stores")
public class StoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_loja", nullable = false)
    private String storeName;

    @Column(name = "data_criacao", nullable = false)
    private Date creationDate;

    @PrePersist
    void onCreate() {
        if (creationDate == null) {
            creationDate = new Date();
        }
    }
}
