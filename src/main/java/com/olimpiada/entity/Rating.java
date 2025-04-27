package com.olimpiada.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "ratings")
@Data
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "olympiad_id", nullable = false)
    private Olympiad olympiad;

    @Column(name = "total_score")
    private Float totalScore;

    @Column
    private Integer place;

    @OneToMany(mappedBy = "rating", cascade = CascadeType.ALL)
    private Set<Result> results;
} 