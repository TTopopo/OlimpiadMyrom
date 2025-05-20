package com.olimpiada.repository;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OlympiadRepository extends JpaRepository<Olympiad, Long> {
    List<Olympiad> findByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);
    List<Olympiad> findByStartDateAfter(LocalDateTime start);
    List<Olympiad> findByEndDateBefore(LocalDateTime end);
} 