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
    public String addOlympiad(@RequestParam String name,
                              @RequestParam String description,
                              @RequestParam Integer age,
                              @RequestParam String educationLevel,
                              @RequestParam Integer courseNumber,
                              @RequestParam String startDate,
                              @RequestParam String endDate,
                              Model model) {
        String error = validateEducationAndCourse(educationLevel, courseNumber);
        if (error != null) {
            model.addAttribute("olympiad", new Olympiad());
            model.addAttribute("error", error);
            return "admin/olympiad-form";
        }
        Olympiad olympiad = new Olympiad();
        olympiad.setName(name);
        olympiad.setDescription(description);
        olympiad.setAge(age);
        olympiad.setEducationLevel(com.olimpiada.entity.CourseType.valueOf(educationLevel));
        olympiad.setCourseNumber(courseNumber);
        olympiad.setStartDate(java.time.LocalDateTime.parse(startDate));
        olympiad.setEndDate(java.time.LocalDateTime.parse(endDate));
        olympiad.setStatus(com.olimpiada.entity.OlympiadStatus.PUBLISHED);
        olympiadService.save(olympiad);
        return "redirect:/admin/olympiads";
    }

    @GetMapping("/edit/{id}")
    public String editOlympiadForm(@PathVariable Long id, Model model) {
        model.addAttribute("olympiad", olympiadService.findById(id));
        return "admin/olympiad-form";
    }

    @PostMapping("/edit/{id}")
    public String editOlympiad(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam String description,
                               @RequestParam Integer age,
                               @RequestParam String educationLevel,
                               @RequestParam Integer courseNumber,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               Model model) {
        String error = validateEducationAndCourse(educationLevel, courseNumber);
        if (error != null) {
            model.addAttribute("olympiad", olympiadService.findById(id));
            model.addAttribute("error", error);
            return "admin/olympiad-form";
        }
        Olympiad olympiad = olympiadService.findById(id);
        olympiad.setName(name);
        olympiad.setDescription(description);
        olympiad.setAge(age);
        olympiad.setEducationLevel(com.olimpiada.entity.CourseType.valueOf(educationLevel));
        olympiad.setCourseNumber(courseNumber);
        olympiad.setStartDate(java.time.LocalDateTime.parse(startDate));
        olympiad.setEndDate(java.time.LocalDateTime.parse(endDate));
        olympiadService.save(olympiad);
        return "redirect:/admin/olympiads";
    }

    @GetMapping("/delete/{id}")
    public String deleteOlympiad(@PathVariable Long id) {
        olympiadService.deleteById(id);
        return "redirect:/admin/olympiads";
    }

    @GetMapping("/{id}")
    public String showOlympiadDetails(@PathVariable Long id, Model model) {
        Olympiad olympiad = olympiadService.findById(id);
        model.addAttribute("olympiad", olympiad);
        return "admin/olympiad-details";
    }

    private String validateEducationAndCourse(String educationLevel, Integer courseNumber) {
        if (educationLevel.equals("BACHELOR") && (courseNumber < 1 || courseNumber > 4)) {
            return "Для бакалавриата допустимы только курсы 1-4";
        }
        if (educationLevel.equals("MASTER") && (courseNumber < 1 || courseNumber > 2)) {
            return "Для магистратуры допустимы только курсы 1-2";
        }
        return null;
    }
} 