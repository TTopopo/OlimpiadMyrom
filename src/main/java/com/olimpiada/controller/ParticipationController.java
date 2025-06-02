package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.Participation;
import com.olimpiada.entity.User;
import com.olimpiada.repository.OlympiadRepository;
import com.olimpiada.repository.ParticipationRepository;
import com.olimpiada.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/participations")
public class ParticipationController {
    private final ParticipationRepository participationRepository;
    private final OlympiadRepository olympiadRepository;
    private final UserRepository userRepository;

    @Autowired
    public ParticipationController(
            ParticipationRepository participationRepository,
            OlympiadRepository olympiadRepository,
            UserRepository userRepository) {
        this.participationRepository = participationRepository;
        this.olympiadRepository = olympiadRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/olympiad/{olympiadId}")
    public String registerForOlympiad(@PathVariable Long olympiadId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            System.out.println("[WARN] Попытка записи на олимпиаду администратором: " + email);
            return "redirect:/olympiads?error=admin_forbidden";
        }
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        boolean alreadyRegistered = participationRepository.findByUserIdAndOlympiadId(user.getId(), olympiadId).isPresent();
        boolean educationMatch = user.getEducationLevel() != null && olympiad.getEducationLevel() != null && user.getEducationLevel().name().equals(olympiad.getEducationLevel().name());
        boolean courseMatch = user.getCourseNumber() != null && olympiad.getCourseNumber() != null && user.getCourseNumber().equals(olympiad.getCourseNumber());
        if (!educationMatch || !courseMatch) {
            System.out.println("[INFO] Пользователь не подходит по уровню/курсу: " + email);
            return "redirect:/olympiads?error=not_allowed";
        }
        if (alreadyRegistered) {
            System.out.println("[INFO] Пользователь уже записан: " + email);
            return "redirect:/olympiads?error=already_registered";
        }
        Participation participation = new Participation();
        participation.setUser(user);
        participation.setOlympiad(olympiad);
        participation.setRegistrationDate(java.time.LocalDateTime.now());
        participationRepository.save(participation);
        System.out.println("[SUCCESS] Пользователь успешно записан: " + email);
        return "redirect:/olympiads?success=registered";
    }

    @GetMapping("/user")
    public String getUserParticipations(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Participation> participations = participationRepository.findByUserId(user.getId());
        model.addAttribute("participations", participations);
        return "participations/list";
    }

    @GetMapping("/olympiad/{olympiadId}/participants")
    @PreAuthorize("hasRole('ADMIN')")
    public String getOlympiadParticipants(@PathVariable Long olympiadId, Model model) {
        List<Participation> participations = participationRepository.findByOlympiadId(olympiadId);
        model.addAttribute("participations", participations);
        model.addAttribute("olympiadId", olympiadId);
        return "participations/participants";
    }

    @GetMapping("/olympiad/{olympiadId}/confirm")
    public String confirmParticipation(
        @PathVariable Long olympiadId,
        @RequestParam(required = false) String error,
        @RequestParam(required = false) String success,
        Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/olympiads?error=admin_forbidden";
        }
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        boolean alreadyRegistered = participationRepository.findByUserIdAndOlympiadId(user.getId(), olympiadId).isPresent();
        boolean olympiadActive = olympiad.getStatus() != null && olympiad.getStatus().name().equals("ACTIVE");
        if (alreadyRegistered && olympiadActive) {
            return "redirect:/user/olympiad/" + olympiadId + "/start?error=already_registered";
        }
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("error", alreadyRegistered && !olympiadActive ? "already_registered" : error);
        model.addAttribute("success", success);
        return "participations/confirm";
    }
} 