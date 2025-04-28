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
        return userAnswerRepository.findByUser_IdAndTask_Olympiad_Id(userId, olympiadId);
    }
    
    public List<UserAnswer> findByOlympiad(Long olympiadId) {
        return userAnswerRepository.findByTask_Olympiad_Id(olympiadId);
    }
    
    public List<UserAnswer> findUncheckedAnswers(Long olympiadId) {
        return userAnswerRepository.findByTask_Olympiad_IdAndScoreIsNull(olympiadId);
    }
    
    public UserAnswer findById(Long id) {
        return userAnswerRepository.findById(id).orElse(null);
    }
    
    public UserAnswer save(UserAnswer userAnswer) {
        return userAnswerRepository.save(userAnswer);
    }
} 