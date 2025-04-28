package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.Task;
import com.olimpiada.entity.User;
import com.olimpiada.entity.UserAnswer;
import com.olimpiada.service.OlympiadService;
import com.olimpiada.service.TaskService;
import com.olimpiada.service.UserAnswerService;
import com.olimpiada.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user/olympiad")
@PreAuthorize("hasRole('USER')")
public class UserOlympiadController {

    @Autowired
    private OlympiadService olympiadService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserAnswerService userAnswerService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public String listOlympiads(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("activeOlympiads", olympiadService.findActiveOlympiads(now));
        model.addAttribute("upcomingOlympiads", olympiadService.findUpcomingOlympiads(now));
        model.addAttribute("pastOlympiads", olympiadService.findPastOlympiads(now));
        return "olympiads/list";
    }

    @GetMapping("/{id}")
    public String viewOlympiad(@PathVariable Long id, Model model, Authentication authentication) {
        Olympiad olympiad = olympiadService.findById(id)
            .orElseThrow(() -> new RuntimeException("Olympiad not found"));
        if (olympiad.getEndDate().isBefore(LocalDateTime.now())) {
            return "redirect:/user/olympiads";
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Task> tasks = taskService.findByOlympiad(id);
        List<UserAnswer> userAnswers = userAnswerService.findByUserAndOlympiad(user.getId(), id);
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("tasks", tasks);
        model.addAttribute("userAnswers", userAnswers);
        return "user/olympiad/view";
    }

    @PostMapping("/{olympiadId}/task/{taskId}/answer")
    public String submitAnswer(@PathVariable Long olympiadId, 
                             @PathVariable Long taskId,
                             @RequestParam String answer,
                             Authentication authentication) {
        Task task = taskService.findById(taskId);
        if (task == null) {
            return "redirect:/user/olympiad/" + olympiadId;
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(user);
        userAnswer.setTask(task);
        userAnswer.setAnswer(answer);
        userAnswerService.save(userAnswer);
        return "redirect:/user/olympiad/" + olympiadId;
    }
} 