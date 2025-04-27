package com.olimpiada.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "answers")
@Data
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column
    private Float score;

    @ManyToOne
    @JoinColumn(name = "result_id")
    private Result result;
} 