package com.olimpiada.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "olympiads")
@Data
public class Olympiad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "olympiad", cascade = CascadeType.ALL)
    private Set<Task> tasks;

    @OneToMany(mappedBy = "olympiad", cascade = CascadeType.ALL)
    private Set<Participation> participations;

    @OneToOne(mappedBy = "olympiad", cascade = CascadeType.ALL)
    private Rating rating;

    @OneToMany(mappedBy = "olympiad", cascade = CascadeType.ALL)
    private Set<Result> results;
} 