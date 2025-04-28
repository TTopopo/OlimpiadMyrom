package com.olimpiada.entity;

import jakarta.persistence.*;
import java.time.Duration;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer maxScore;

    @ManyToOne
    @JoinColumn(name = "olympiad_id", nullable = false)
    private Olympiad olympiad;

    @Column(nullable = false)
    private Duration timeLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<UserAnswer> userAnswers;

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

    public String getTaskText() {
        return description;
    }

    public void setTaskText(String taskText) {
        this.description = taskText;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Duration getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Duration timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<UserAnswer> getAnswers() {
        return userAnswers;
    }

    public void setAnswers(List<UserAnswer> answers) {
        this.userAnswers = answers;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }
} 