package com.olimpiada.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participations")
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "olympiad_id", nullable = false)
    private Olympiad olympiad;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

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

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
} 