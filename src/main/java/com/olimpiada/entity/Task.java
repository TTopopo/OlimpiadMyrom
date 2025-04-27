package com.olimpiada.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "olympiad_id", nullable = false)
    private Olympiad olympiad;

    @Column(name = "task_text", columnDefinition = "TEXT", nullable = false)
    private String taskText;

    @Column(name = "task_type", nullable = false)
    private String taskType;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private Set<Answer> answers;
} 