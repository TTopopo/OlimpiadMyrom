package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.UserAnswer;
import com.olimpiada.service.OlympiadService;
import com.olimpiada.service.UserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/rating")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRatingController {

    @Autowired
    private OlympiadService olympiadService;

    @Autowired
    private UserAnswerService userAnswerService;

    @GetMapping
    public String showRating(Model model) {
        List<Olympiad> olympiads = olympiadService.findAll();
        model.addAttribute("olympiads", olympiads);
        return "admin/rating/list";
    }

    @GetMapping("/{olympiadId}")
    public String showOlympiadRating(@PathVariable Long olympiadId, Model model) {
        Olympiad olympiad = olympiadService.findById(olympiadId);
        List<UserAnswer> answers = userAnswerService.findByOlympiad(olympiadId);

        // Группируем ответы по пользователям и считаем общий балл
        Map<String, Integer> userScores = answers.stream()
            .collect(Collectors.groupingBy(
                answer -> answer.getUser().getFullName(),
                Collectors.summingInt(UserAnswer::getScore)
            ));

        // Сортируем пользователей по баллам и присваиваем места
        List<Map.Entry<String, Integer>> sortedScores = userScores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());

        // Добавляем места для первых трех участников
        for (int i = 0; i < Math.min(3, sortedScores.size()); i++) {
            Map.Entry<String, Integer> entry = sortedScores.get(i);
            String place = switch (i) {
                case 0 -> "🥇 Первое место";
                case 1 -> "🥈 Второе место";
                case 2 -> "🥉 Третье место";
                default -> "";
            };
            entry.setValue(entry.getValue() + 1000); // Добавляем 1000 к баллам для отображения места
        }

        model.addAttribute("olympiad", olympiad);
        model.addAttribute("scores", sortedScores);
        return "admin/rating/olympiad";
    }
} 