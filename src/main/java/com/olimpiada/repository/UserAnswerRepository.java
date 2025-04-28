package com.olimpiada.repository;

import com.olimpiada.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByUser_IdAndTask_Olympiad_Id(Long userId, Long olympiadId);
    List<UserAnswer> findByTask_Olympiad_Id(Long olympiadId);
    List<UserAnswer> findByTask_Olympiad_IdAndScoreIsNull(Long olympiadId);
} 