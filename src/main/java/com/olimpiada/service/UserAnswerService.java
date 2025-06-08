package com.olimpiada.service;

import com.olimpiada.entity.UserAnswer;
import com.olimpiada.repository.UserAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserAnswerService {
    
    @Autowired
    private UserAnswerRepository userAnswerRepository;
    
    public UserAnswer findById(Long id) {
        return userAnswerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserAnswer not found with id: " + id));
    }
    
    public List<UserAnswer> findByUserId(Long userId) {
        return userAnswerRepository.findByUserId(userId);
    }
    
    public List<UserAnswer> findByUserAndOlympiad(Long userId, Long olympiadId) {
        return userAnswerRepository.findByUserIdAndTaskOlympiadId(userId, olympiadId);
    }
    
    public List<UserAnswer> findByOlympiad(Long olympiadId) {
        return userAnswerRepository.findByTaskOlympiadId(olympiadId);
    }
    
    public List<UserAnswer> findUncheckedAnswers(Long olympiadId) {
        return userAnswerRepository.findByTaskOlympiadIdAndScoreIsNull(olympiadId);
    }
    
    public UserAnswer save(UserAnswer userAnswer) {
        return userAnswerRepository.save(userAnswer);
    }
    
    public Optional<UserAnswer> findByUserAndTask(Long userId, Long taskId) {
        return userAnswerRepository.findByUserIdAndTaskId(userId, taskId);
    }
    
    public void deleteAllByUserAndOlympiad(Long userId, Long olympiadId) {
        List<UserAnswer> answers = findByUserAndOlympiad(userId, olympiadId);
        userAnswerRepository.deleteAll(answers);
    }
} 