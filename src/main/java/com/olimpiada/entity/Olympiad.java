package com.olimpiada.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString(exclude = "tasks")
@Entity
@Table(name = "olympiads")
public class Olympiad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OlympiadStatus status = OlympiadStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseType educationLevel;

    @Column(nullable = false)
    private Integer courseNumber;

    @OneToMany(mappedBy = "olympiad", cascade = CascadeType.ALL)
    private List<Task> tasks;

    @OneToMany(mappedBy = "olympiad", cascade = CascadeType.ALL)
    private List<Participation> participations;

    @OneToOne(mappedBy = "olympiad", cascade = CascadeType.ALL)
    private Rating rating;

    @OneToMany(mappedBy = "olympiad", cascade = CascadeType.ALL)
    private List<Result> results;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Participation> getParticipations() {
        return participations;
    }

    public void setParticipations(List<Participation> participations) {
        this.participations = participations;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public OlympiadStatus getStatus() {
        return status;
    }

    public void setStatus(OlympiadStatus status) {
        this.status = status;
    }

    public CourseType getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(CourseType educationLevel) {
        this.educationLevel = educationLevel;
    }

    public Integer getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(Integer courseNumber) {
        this.courseNumber = courseNumber;
    }
} 