package com.olimpiada.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ratings")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "olympiad_id", nullable = false)
    private Olympiad olympiad;

    @Column(nullable = false)
    private Float totalScore;

    @OneToMany(mappedBy = "rating", cascade = CascadeType.ALL)
    private List<Result> results;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Olympiad getOlympiad() {
        return olympiad;
    }

    public void setOlympiad(Olympiad olympiad) {
        this.olympiad = olympiad;
    }

    public Float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Float totalScore) {
        this.totalScore = totalScore;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
} 