package com.olimpiada.service;

import com.olimpiada.entity.UserAnswer;
import com.olimpiada.repository.UserAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAnswerService {
    
    @Autowired
    private UserAnswerRepository userAnswerRepository;
    
    public List<UserAnswer> findByUserAndOlympiad(Long userId, Long olympiadId) {
        return userAnswerRepository.findByUserIdAndOlympiadId(userId, olympiadId);
    }
    
    public List<UserAnswer> findByOlympiad(Long olympiadId) {
        return userAnswerRepository.findByOlympiadId(olympiadId);
    }
    
    public List<UserAnswer> findUncheckedAnswers(Long olympiadId) {
        return userAnswerRepository.findByOlympiadIdAndScoreIsNull(olympiadId);
    }
    
    public UserAnswer findById(Long id) {
        return userAnswerRepository.findById(id).orElse(null);
    }
    
    public UserAnswer save(UserAnswer userAnswer) {
        return userAnswerRepository.save(userAnswer);
    }
} 