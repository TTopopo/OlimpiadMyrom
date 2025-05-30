package com.olimpiada.controller;

import com.olimpiada.entity.UserAnswer;
import com.olimpiada.service.UserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-answers")
public class UserAnswerController {
    
    @Autowired
    private UserAnswerService userAnswerService;
    
    @GetMapping("/user/{userId}/olympiad/{olympiadId}")
    public List<UserAnswer> getUserAnswers(@PathVariable Long userId, @PathVariable Long olympiadId) {
        return userAnswerService.findByUserAndOlympiad(userId, olympiadId);
    }
    
    @GetMapping("/olympiad/{olympiadId}")
    public List<UserAnswer> getOlympiadAnswers(@PathVariable Long olympiadId) {
        return userAnswerService.findByOlympiad(olympiadId);
    }
    
    @GetMapping("/unchecked/{olympiadId}")
    public List<UserAnswer> getUncheckedAnswers(@PathVariable Long olympiadId) {
        return userAnswerService.findUncheckedAnswers(olympiadId);
    }
    
    @PostMapping
    public UserAnswer createUserAnswer(@RequestBody UserAnswer userAnswer) {
        return userAnswerService.save(userAnswer);
    }
    
    @PutMapping("/{id}/score")
    public ResponseEntity<UserAnswer> updateScore(@PathVariable Long id, @RequestBody Integer score) {
        UserAnswer userAnswer = userAnswerService.findById(id);
        if (userAnswer != null) {
            userAnswer.setScore(score);
            return ResponseEntity.ok(userAnswerService.save(userAnswer));
        }
        return ResponseEntity.notFound().build();
    }
} 