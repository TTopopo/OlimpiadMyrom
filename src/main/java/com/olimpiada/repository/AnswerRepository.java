package com.olimpiada.repository;

import com.olimpiada.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByTaskId(Long taskId);
    List<Answer> findByUserId(Long userId);
    List<Answer> findByTaskIdAndUserId(Long taskId, Long userId);
    List<Answer> findByUserIdAndTaskOlympiadId(Long userId, Long olympiadId);
} 