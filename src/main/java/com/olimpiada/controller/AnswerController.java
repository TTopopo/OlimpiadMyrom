package com.olimpiada.controller;

import com.olimpiada.entity.UserAnswer;
import com.olimpiada.entity.Task;
import com.olimpiada.entity.User;
import com.olimpiada.service.UserAnswerService;
import com.olimpiada.repository.TaskRepository;
import com.olimpiada.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/answers")
public class AnswerController {
    private final UserAnswerService userAnswerService;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public AnswerController(
            UserAnswerService userAnswerService,
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.userAnswerService = userAnswerService;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/task/{taskId}")
    public String submitAnswer(
            @PathVariable Long taskId,
            @RequestParam String answerText) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Задание не найдено"));

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(user);
        userAnswer.setTask(task);
        userAnswer.setAnswer(answerText);
        userAnswer.setScore(null); // Оценка будет выставлена позже администратором
        userAnswer.setSubmissionTime(LocalDateTime.now());

        userAnswerService.save(userAnswer);

        return "redirect:/api/tasks/" + taskId + "?success=answer_submitted";
    }

    @GetMapping("/task/{taskId}")
    public String getTaskAnswers(@PathVariable Long taskId, Model model) {
        List<UserAnswer> answers = userAnswerService.findByTaskId(taskId);
        model.addAttribute("answers", answers);
        model.addAttribute("taskId", taskId);
        return "answers/list";
    }

    @GetMapping("/user")
    public String getUserAnswers(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<UserAnswer> answers = userAnswerService.findByUserId(user.getId());
        model.addAttribute("answers", answers);
        return "answers/user-list";
    }

    @PostMapping("/{id}/evaluate")
    @PreAuthorize("hasRole('ADMIN')")
    public String evaluateAnswer(
            @PathVariable Long id,
            @RequestParam Integer score) {
        UserAnswer answer = userAnswerService.findById(id);
        answer.setScore(score);
        userAnswerService.save(answer);

        return "redirect:/api/answers/task/" + answer.getTask().getId();
    }
} 