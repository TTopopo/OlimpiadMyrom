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
    public String registerForOlympiad(@PathVariable Long olympiadId, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));

        boolean alreadyRegistered = participationRepository.findByUserIdAndOlympiadId(user.getId(), olympiadId).isPresent();

        if (olympiad.getStatus() == com.olimpiada.entity.OlympiadStatus.ACTIVE) {
            if (alreadyRegistered) {
                return "redirect:/user/olympiad/" + olympiadId + "/start?error=already_registered";
            } else {
                Participation participation = new Participation();
                participation.setUser(user);
                participation.setOlympiad(olympiad);
                participation.setRegistrationDate(java.time.LocalDateTime.now());
                participationRepository.save(participation);
                return "redirect:/user/olympiad/" + olympiadId + "/start";
            }
        } else {
            if (alreadyRegistered) {
                redirectAttributes.addAttribute("error", "already_registered");
                return "redirect:/olympiads";
            } else {
        Participation participation = new Participation();
        participation.setUser(user);
        participation.setOlympiad(olympiad);
        participation.setRegistrationDate(java.time.LocalDateTime.now());
        participationRepository.save(participation);
                redirectAttributes.addAttribute("success", "registered");
                redirectAttributes.addAttribute("olympiadName", olympiad.getName());
                return "redirect:/olympiads";
            }
        }
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
    public String confirmParticipation(@PathVariable Long olympiadId, Model model) {
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        model.addAttribute("olympiad", olympiad);
        return "participations/confirm";
    }
} 