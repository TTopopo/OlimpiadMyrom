package com.olimpiada.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "olympiad")
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "task_text", nullable = false, columnDefinition = "TEXT")
    private String taskText;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    @ManyToOne
    @JoinColumn(name = "olympiad_id", nullable = false)
    private Olympiad olympiad;

    @Transient
    private String displayCorrectAnswer;

    @Column(name = "image_path")
    private String imagePath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public Olympiad getOlympiad() {
        return olympiad;
    }

    public void setOlympiad(Olympiad olympiad) {
        this.olympiad = olympiad;
    }

    @jakarta.persistence.Transient
    public java.util.List<String> getOptionsList() {
        if (options == null || options.isEmpty()) return java.util.List.of();
        return java.util.Arrays.stream(options.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public String getDisplayCorrectAnswer() {
        return displayCorrectAnswer;
    }

    public void setDisplayCorrectAnswer(String displayCorrectAnswer) {
        this.displayCorrectAnswer = displayCorrectAnswer;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
} 