package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.UserAnswer;
import com.olimpiada.entity.User;
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
        Map<User, Integer> userScores = answers.stream()
            .filter(answer -> answer.getUser() != null && answer.getUser().getFullName() != null)
            .collect(Collectors.groupingBy(
                UserAnswer::getUser,
                Collectors.summingInt(ans -> ans.getScore() != null ? ans.getScore() : 0)
            ));

        // Сортируем пользователей по баллам и присваиваем места
        List<Map.Entry<User, Integer>> sortedScores = userScores.entrySet().stream()
            .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());

        model.addAttribute("olympiad", olympiad);
        model.addAttribute("scores", sortedScores);
        return "admin/rating/olympiad";
    }
} 