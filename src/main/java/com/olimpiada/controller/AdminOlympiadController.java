package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.service.OlympiadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/olympiads")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOlympiadController {

    @Autowired
    private OlympiadService olympiadService;

    @GetMapping
    public String listOlympiads(Model model) {
        model.addAttribute("olympiads", olympiadService.findAll());
        return "admin/olympiads";
    }

    @GetMapping("/add")
    public String addOlympiadForm(Model model) {
        model.addAttribute("olympiad", new Olympiad());
        return "admin/olympiad-form";
    }

    @PostMapping("/add")
    public String addOlympiad(@ModelAttribute Olympiad olympiad) {
        olympiadService.save(olympiad);
        return "redirect:/admin/olympiads";
    }

    @GetMapping("/edit/{id}")
    public String editOlympiadForm(@PathVariable Long id, Model model) {
        model.addAttribute("olympiad", olympiadService.findById(id));
        return "admin/olympiad-form";
    }

    @PostMapping("/edit/{id}")
    public String editOlympiad(@PathVariable Long id, @ModelAttribute Olympiad olympiad) {
        olympiad.setId(id);
        olympiadService.save(olympiad);
        return "redirect:/admin/olympiads";
    }

    @GetMapping("/delete/{id}")
    public String deleteOlympiad(@PathVariable Long id) {
        olympiadService.deleteById(id);
        return "redirect:/admin/olympiads";
    }
} 