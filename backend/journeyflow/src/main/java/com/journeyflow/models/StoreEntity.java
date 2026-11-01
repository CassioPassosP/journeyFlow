package com.journeyflow.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "stores")
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_loja", nullable = false)
    private String storeName;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime creationDate;

    @PrePersist
    void onCreate() {
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }
    }
}