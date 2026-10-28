package com.journeyflow.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
public class ManagerEntity extends PatternWorkerEntity{

    @Column(nullable = false)
    String email;

    @Column(name = "senha_hash", nullable = false)
    String password;
}
