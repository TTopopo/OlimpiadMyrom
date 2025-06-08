package com.olimpiada.repository;

import com.olimpiada.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByUserId(Long userId);
    List<Participation> findByOlympiadId(Long olympiadId);
    @Query("SELECT p FROM Participation p WHERE p.user.id = :userId AND p.olympiad.id = :olympiadId")
    Optional<Participation> findByUserIdAndOlympiadId(@Param("userId") Long userId, @Param("olympiadId") Long olympiadId);
} 