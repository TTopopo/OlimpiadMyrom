package com.olimpiada.repository;

import com.olimpiada.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByUserId(Long userId);
    List<Participation> findByOlympiadId(Long olympiadId);
    Optional<Participation> findByUserIdAndOlympiadId(Long userId, Long olympiadId);
} 