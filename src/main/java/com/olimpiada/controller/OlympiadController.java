package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.repository.OlympiadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/olympiads")
public class OlympiadController {
    private final OlympiadRepository olympiadRepository;

    @Autowired
    public OlympiadController(OlympiadRepository olympiadRepository) {
        this.olympiadRepository = olympiadRepository;
    }

    @GetMapping
    public String getAllOlympiads(Model model) {
        List<Olympiad> olympiads = olympiadRepository.findAll();
        model.addAttribute("olympiads", olympiads);
        return "olympiads/list";
    }

    @GetMapping("/{id}")
    public String getOlympiad(@PathVariable Long id, Model model) {
        Olympiad olympiad = olympiadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        model.addAttribute("olympiad", olympiad);
        return "olympiads/view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createOlympiadForm(Model model) {
        model.addAttribute("olympiad", new Olympiad());
        return "olympiads/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createOlympiad(@ModelAttribute Olympiad olympiad) {
        olympiad.setStatus("PLANNED");
        olympiadRepository.save(olympiad);
        return "redirect:/api/olympiads";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editOlympiadForm(@PathVariable Long id, Model model) {
        Olympiad olympiad = olympiadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        model.addAttribute("olympiad", olympiad);
        return "olympiads/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateOlympiad(@PathVariable Long id, @ModelAttribute Olympiad updatedOlympiad) {
        Olympiad olympiad = olympiadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        
        olympiad.setName(updatedOlympiad.getName());
        olympiad.setDescription(updatedOlympiad.getDescription());
        olympiad.setStartDate(updatedOlympiad.getStartDate());
        olympiad.setEndDate(updatedOlympiad.getEndDate());
        olympiad.setAge(updatedOlympiad.getAge());
        
        olympiadRepository.save(olympiad);
        return "redirect:/api/olympiads";
    }

    @GetMapping("/active")
    public String getActiveOlympiads(Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<Olympiad> activeOlympiads = olympiadRepository
            .findByStartDateBeforeAndEndDateAfter(now, now);
        model.addAttribute("olympiads", activeOlympiads);
        return "olympiads/list";
    }

    @GetMapping("/upcoming")
    public String getUpcomingOlympiads(Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<Olympiad> upcomingOlympiads = olympiadRepository.findByStartDateAfter(now);
        model.addAttribute("olympiads", upcomingOlympiads);
        return "olympiads/list";
    }

    @GetMapping("/past")
    public String getPastOlympiads(Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<Olympiad> pastOlympiads = olympiadRepository.findByEndDateBefore(now);
        model.addAttribute("olympiads", pastOlympiads);
        return "olympiads/list";
    }
} 