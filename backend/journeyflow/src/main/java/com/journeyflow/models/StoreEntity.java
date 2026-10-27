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
    long id;

    @Column(name = "nome_loja", nullable = false)
    String storeName;

    @Column(name = "data_criacao", nullable = false)
    Date creationDate;
}
