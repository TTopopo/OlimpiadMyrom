package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.UserAnswer;
import com.olimpiada.service.OlympiadService;
import com.olimpiada.service.UserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/results")
@PreAuthorize("hasRole('ADMIN')")
public class AdminResultsController {

    @Autowired
    private OlympiadService olympiadService;
    
    @Autowired
    private UserAnswerService userAnswerService;

    @GetMapping
    public String listOlympiads(Model model) {
        model.addAttribute("olympiads", olympiadService.findAll());
        return "admin/results/list";
    }

    @GetMapping("/{olympiadId}")
    public String viewResults(@PathVariable Long olympiadId, Model model) {
        Olympiad olympiad = olympiadService.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Olympiad not found"));
        List<UserAnswer> answers = userAnswerService.findByOlympiad(olympiadId);
        
        // Группируем ответы по пользователям и считаем общий балл
        Map<String, Integer> userScores = answers.stream()
            .collect(Collectors.groupingBy(
                answer -> answer.getUser().getEmail(),
                Collectors.summingInt(UserAnswer::getScore)
            ));
        
        // Сортируем пользователей по баллам
        List<Map.Entry<String, Integer>> sortedResults = userScores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());
        
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("results", sortedResults);
        return "admin/results/view";
    }
} 