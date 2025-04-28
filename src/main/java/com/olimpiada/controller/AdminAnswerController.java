package com.olimpiada.controller;

import com.olimpiada.entity.UserAnswer;
import com.olimpiada.service.UserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/answers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnswerController {

    @Autowired
    private UserAnswerService userAnswerService;

    @GetMapping("/{olympiadId}")
    public String listUncheckedAnswers(@PathVariable Long olympiadId, Model model) {
        model.addAttribute("answers", userAnswerService.findUncheckedAnswers(olympiadId));
        return "admin/answers/list";
    }

    @GetMapping("/check/{answerId}")
    public String checkAnswerForm(@PathVariable Long answerId, Model model) {
        model.addAttribute("answer", userAnswerService.findById(answerId));
        return "admin/answers/check";
    }

    @PostMapping("/check/{answerId}")
    public String checkAnswer(@PathVariable Long answerId, @RequestParam Integer score) {
        UserAnswer answer = userAnswerService.findById(answerId);
        answer.setScore(score);
        userAnswerService.save(answer);
        return "redirect:/admin/answers/" + answer.getTask().getOlympiad().getId();
    }
} 