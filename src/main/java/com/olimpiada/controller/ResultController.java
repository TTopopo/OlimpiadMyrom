package com.olimpiada.controller;

import com.olimpiada.entity.*;
import com.olimpiada.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/results")
public class ResultController {
    private final ResultRepository resultRepository;
    private final AnswerRepository answerRepository;
    private final OlympiadRepository olympiadRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    @Autowired
    public ResultController(
            ResultRepository resultRepository,
            AnswerRepository answerRepository,
            OlympiadRepository olympiadRepository,
            UserRepository userRepository,
            RatingRepository ratingRepository) {
        this.resultRepository = resultRepository;
        this.answerRepository = answerRepository;
        this.olympiadRepository = olympiadRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping("/olympiad/{olympiadId}")
    public String getOlympiadResults(@PathVariable Long olympiadId, Model model) {
        List<Result> results = resultRepository.findByOlympiadId(olympiadId);
        model.addAttribute("results", results);
        model.addAttribute("olympiadId", olympiadId);
        return "results/olympiad";
    }

    @GetMapping("/user")
    public String getUserResults(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Result> results = resultRepository.findByUserId(user.getId());
        model.addAttribute("results", results);
        return "results/user-list";
    }

    @PostMapping("/olympiad/{olympiadId}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public String calculateResults(@PathVariable Long olympiadId) {
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));

        // Получаем всех участников олимпиады
        List<Participation> participations = olympiad.getParticipations().stream()
            .collect(Collectors.toList());

        for (Participation participation : participations) {
            User user = participation.getUser();
            
            // Получаем все ответы пользователя по олимпиаде
            List<Answer> answers = answerRepository.findByUserIdAndTaskOlympiadId(user.getId(), olympiadId);
            
            // Вычисляем общий балл
            Float totalScore = answers.stream()
                .filter(answer -> answer.getScore() != null)
                .map(Answer::getScore)
                .reduce(0f, Float::sum);

            // Создаем или обновляем результат
            Result result = resultRepository.findByUserIdAndOlympiadId(user.getId(), olympiadId)
                .stream()
                .findFirst()
                .orElse(new Result());
            
            result.setUser(user);
            result.setOlympiad(olympiad);
            result.setTotalScore(totalScore);
            result.setSubmissionDate(LocalDateTime.now());
            result.setStatus(ResultStatus.COMPLETED);

            resultRepository.save(result);
        }

        // Обновляем рейтинг
        updateRating(olympiadId);

        return "redirect:/api/results/olympiad/" + olympiadId;
    }

    private void updateRating(Long olympiadId) {
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));

        // Получаем все результаты по олимпиаде, отсортированные по убыванию баллов
        List<Result> results = resultRepository.findByOlympiadId(olympiadId).stream()
            .sorted((r1, r2) -> Float.compare(r2.getTotalScore(), r1.getTotalScore()))
            .collect(Collectors.toList());

        // Создаем или обновляем рейтинг
        Rating rating = ratingRepository.findByOlympiadId(olympiadId)
            .stream()
            .findFirst()
            .orElse(new Rating());
        
        rating.setOlympiad(olympiad);
        rating.setTotalScore(results.stream()
            .map(Result::getTotalScore)
            .reduce(0f, Float::sum));

        ratingRepository.save(rating);

        // Обновляем места участников
        for (int i = 0; i < results.size(); i++) {
            Result result = results.get(i);
            result.setPlace(i + 1);
            result.setRating(rating);
            resultRepository.save(result);
        }
    }

    @PostMapping("/{id}/status")
    public String updateResultStatus(@PathVariable Long id, @RequestParam ResultStatus status) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Result not found"));
        result.setStatus(status);
        resultRepository.save(result);
        return "redirect:/api/results/" + id;
    }
} 