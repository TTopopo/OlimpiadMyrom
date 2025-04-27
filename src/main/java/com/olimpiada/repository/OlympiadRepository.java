package com.olimpiada.repository;

import com.olimpiada.entity.Olympiad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OlympiadRepository extends JpaRepository<Olympiad, Long> {
    List<Olympiad> findByStartDateBeforeAndEndDateAfter(LocalDateTime currentTime, LocalDateTime currentTime2);
    List<Olympiad> findByEndDateBefore(LocalDateTime currentTime);
    List<Olympiad> findByStartDateAfter(LocalDateTime currentTime);
    List<Olympiad> findByAgeLessThanEqual(Integer age);
} 