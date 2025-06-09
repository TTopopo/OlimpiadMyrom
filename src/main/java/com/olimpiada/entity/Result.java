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

    @Column(name = "total_score", nullable = false)
    private Float totalScore;

    @Column(name = "max_possible_score", nullable = false)
    private Float maxPossibleScore;

    @Column(name = "completion_percentage", nullable = false)
    private Float completionPercentage;

    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResultStatus status;

    @Column
    private Integer place;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL)
    private Set<UserAnswer> answers;

    @ManyToOne
    @JoinColumn(name = "rating_id")
    private Rating rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Float getMaxPossibleScore() {
        return maxPossibleScore;
    }

    public void setMaxPossibleScore(Float maxPossibleScore) {
        this.maxPossibleScore = maxPossibleScore;
    }

    public Float getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Float completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public Integer getPlace() {
        return place;
    }

    public void setPlace(Integer place) {
        this.place = place;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }
} 