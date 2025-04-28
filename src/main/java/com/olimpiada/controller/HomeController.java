package com.olimpiada.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

@Controller
public class HomeController {
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
        model.addAttribute("title", "Начать участие");
        return "start";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("title", "Профиль");
        return "profile";
    }

    @GetMapping("/olympiads")
    public String olympiads(Model model) {
        model.addAttribute("title", "Олимпиады");
        return "olympiads";
    }

    @GetMapping("/results")
    public String results(Model model) {
        model.addAttribute("title", "Результаты");
        return "results";
    }

    @GetMapping("/rating")
    public String rating(Model model) {
        model.addAttribute("title", "Рейтинг");
        return "rating";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("title", "Панель администратора");
        return "admin";
    }
} 