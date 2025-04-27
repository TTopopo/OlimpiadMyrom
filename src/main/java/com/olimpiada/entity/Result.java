package com.olimpiada.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "results")
@Data
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "olympiad_id", nullable = false)
    private Olympiad olympiad;

    @Column(name = "total_score")
    private Float totalScore;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column
    private String status;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL)
    private Set<Answer> answers;

    @ManyToOne
    @JoinColumn(name = "rating_id")
    private Rating rating;
} 