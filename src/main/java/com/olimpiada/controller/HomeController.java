package com.olimpiada.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import com.olimpiada.service.OlympiadService;
import com.olimpiada.entity.Olympiad;
import com.olimpiada.service.TaskService;
import org.springframework.web.bind.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private OlympiadService olympiadService;

    @Autowired
    private TaskService taskService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Главная страница");
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/start")
    public String start(Model model) {
        return "redirect:/olympiads";
    }

    @GetMapping("/olympiads")
    public String listOlympiads(Model model, @RequestParam(value = "success", required = false) String success, @RequestParam(value = "error", required = false) String error, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin?error=admin_forbidden";
        }
        model.addAttribute("title", "Олимпиады");
        model.addAttribute("activeOlympiads", olympiadService.findActiveOlympiads());
        model.addAttribute("upcomingOlympiads", olympiadService.findUpcomingOlympiads(java.time.LocalDateTime.now()));
        model.addAttribute("pastOlympiads", olympiadService.findPastOlympiads(java.time.LocalDateTime.now()));
        if (success != null && !success.isEmpty()) model.addAttribute("success", success);
        if (error != null && !error.isEmpty()) model.addAttribute("error", error);
        return "olympiads/list";
    }

    @GetMapping("/olympiads/{id}")
    public String viewOlympiad(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin?error=admin_forbidden";
        }
        String userInfo = (authentication == null) ? "Гость" : authentication.getName();
        logger.info("[Просмотр олимпиады] id={}, пользователь={}", id, userInfo);
        Olympiad olympiad = olympiadService.findById(id);
        if (olympiad.getEndDate().isBefore(LocalDateTime.now())) {
            return "redirect:/olympiads";
        }
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("tasks", taskService.getTasksByOlympiadId(id));
        return "olympiads/view";
    }

    @GetMapping("/results")
    @PreAuthorize("isAuthenticated()")
    public String results(Model model) {
        model.addAttribute("title", "Результаты");
        return "results";
    }

    @GetMapping("/rating")
    @PreAuthorize("isAuthenticated()")
    public String rating(Model model) {
        model.addAttribute("title", "Рейтинг");
        return "rating";
    }

//    @GetMapping("/admin")
//    @PreAuthorize("hasRole('ADMIN')")
//    public String admin(Model model) {
//        model.addAttribute("title", "Панель администратора");
//        return "admin";
//    }
} 