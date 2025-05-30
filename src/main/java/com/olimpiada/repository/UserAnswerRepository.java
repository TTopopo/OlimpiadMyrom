package com.olimpiada.repository;

import com.olimpiada.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByUserId(Long userId);
    List<UserAnswer> findByUserIdAndTaskOlympiadId(Long userId, Long olympiadId);
    List<UserAnswer> findByTaskOlympiadId(Long olympiadId);
    List<UserAnswer> findByTaskOlympiadIdAndScoreIsNull(Long olympiadId);
} 