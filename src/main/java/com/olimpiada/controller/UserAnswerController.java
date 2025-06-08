package com.olimpiada.controller;

import com.olimpiada.entity.UserAnswer;
import com.olimpiada.service.UserAnswerService;
import com.olimpiada.service.TaskService;
import com.olimpiada.service.UserService;
import com.olimpiada.entity.Task;
import com.olimpiada.entity.User;
import com.olimpiada.dto.UserAnswerDTO;
import com.olimpiada.repository.ParticipationRepository;
import com.olimpiada.entity.Participation;
import com.olimpiada.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-answers")
public class UserAnswerController {
    
    @Autowired
    private UserAnswerService userAnswerService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ParticipationRepository participationRepository;
    
    @Autowired
    private ResultService resultService;
    
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
    public UserAnswer createUserAnswer(@RequestBody UserAnswerDTO dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User must be provided");
        }
        if (dto.getTaskId() == null) {
            throw new IllegalArgumentException("Task id must be provided");
        }
        User user = userService.findById(dto.getUserId());
        Task task = taskService.getTaskById(dto.getTaskId());
        java.util.Optional<UserAnswer> existing = userAnswerService.findByUserAndTask(user.getId(), task.getId());
        UserAnswer userAnswer = existing.orElseGet(UserAnswer::new);
        userAnswer.setUser(user);
        userAnswer.setTask(task);
        if (dto.getAnswer() != null) userAnswer.setAnswer(dto.getAnswer());
        if (dto.getFlagged() != null) userAnswer.setFlagged(dto.getFlagged());
        userAnswer.setSubmissionTime(java.time.LocalDateTime.now());
        if (task.getTaskType() != null && userAnswer.getAnswer() != null) {
            if (task.getTaskType().name().equals("SINGLE_CHOICE")) {
                String correct = task.getCorrectAnswer();
                String[] options = task.getOptions() != null ? task.getOptions().split(";") : new String[0];
                int correctIdx = -1;
                try { correctIdx = Integer.parseInt(correct); } catch (Exception ignored) {}
                if (correctIdx >= 0 && correctIdx < options.length) {
                    String correctValue = options[correctIdx];
                    if (userAnswer.getAnswer().equals(correctValue)) {
                        userAnswer.setScore(task.getMaxScore() != null ? Math.round(task.getMaxScore()) : 0);
                    } else {
                        userAnswer.setScore(0);
                    }
                } else {
                    userAnswer.setScore(0);
                }
            } else if (task.getTaskType().name().equals("MULTIPLE_CHOICE")) {
                String correct = task.getCorrectAnswer();
                String[] correctIdxs = correct != null ? correct.split(";") : new String[0];
                String[] options = task.getOptions() != null ? task.getOptions().split(";") : new String[0];
                java.util.Set<String> correctValues = new java.util.HashSet<>();
                for (String idxStr : correctIdxs) {
                    try {
                        int idx = Integer.parseInt(idxStr);
                        if (idx >= 0 && idx < options.length) correctValues.add(options[idx]);
                    } catch (Exception ignored) {}
                }
                String[] userValues = userAnswer.getAnswer().split(",");
                java.util.Set<String> userSet = new java.util.HashSet<>();
                for (String v : userValues) userSet.add(v.trim());
                if (userSet.isEmpty()) {
                    userAnswer.setScore(0);
                } else if (userSet.equals(correctValues)) {
                    userAnswer.setScore(task.getMaxScore() != null ? Math.round(task.getMaxScore()) : 0);
                } else if (correctValues.containsAll(userSet) && !userSet.isEmpty()) {
                    // Все выбранные — правильные, но не все правильные выбраны
                    float part = (float) userSet.size() / (float) correctValues.size();
                    int score = task.getMaxScore() != null ? Math.round(task.getMaxScore() * part) : 0;
                    userAnswer.setScore(score);
                } else {
                    // Есть лишние ответы — 0 баллов
                    userAnswer.setScore(0);
                }
            } else if (task.getTaskType().name().equals("TEXT_ANSWER")) {
                String correct = task.getCorrectAnswer();
                if (correct != null && userAnswer.getAnswer() != null &&
                    userAnswer.getAnswer().trim().equalsIgnoreCase(correct.trim())) {
                    userAnswer.setScore(task.getMaxScore() != null ? Math.round(task.getMaxScore()) : 0);
                } else {
                    userAnswer.setScore(0);
                }
            } else if (task.getTaskType().name().equals("CODE_ANSWER")) {
                String correct = task.getCorrectAnswer();
                if (correct != null && userAnswer.getAnswer() != null &&
                    userAnswer.getAnswer().trim().equals(correct.trim())) {
                    userAnswer.setScore(task.getMaxScore() != null ? Math.round(task.getMaxScore()) : 0);
                } else {
                    userAnswer.setScore(0);
                }
            } else {
                // ... существующий код для других типов ...
            }
        }
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

    @PostMapping("/{id}/flag")
    public ResponseEntity<?> setFlag(@PathVariable Long id, @RequestParam boolean flagged) {
        UserAnswer userAnswer = userAnswerService.findById(id);
        if (userAnswer != null) {
            userAnswer.setFlagged(flagged);
            userAnswerService.save(userAnswer);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/finish-olympiad")
    public ResponseEntity<String> finishOlympiad(@RequestParam Long userId, @RequestParam Long olympiadId) {
        Participation participation = participationRepository.findByUserIdAndOlympiadId(userId, olympiadId).orElse(null);
        if (participation != null) {
            participation.setFinished(true);
            participationRepository.save(participation);
            resultService.calculateAndSaveResultForParticipation(participation);
            resultService.updateRatingByOlympiadId(participation.getOlympiad().getId());
            return ResponseEntity.ok("Олимпиада завершена");
        }
        return ResponseEntity.badRequest().body("Участие не найдено");
    }
} 