package com.journeyflow.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "pills")
public class PillsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "day_from")
    private Integer day_from;

    @Column(name = "quiz_question", nullable = false)
    private String quiz_question;

    @Column(name = "quiz_options", nullable = false)
    private String quiz_options;

    @Column(name = "quiz_correct", nullable = false)
    private Integer quiz_correct;
}
