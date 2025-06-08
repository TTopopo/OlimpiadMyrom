package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.service.OlympiadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/admin/olympiads")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOlympiadController {

    @Autowired
    private OlympiadService olympiadService;

    @GetMapping
    public String listOlympiads(@RequestParam(value = "search", required = false) String search, Model model) {
        olympiadService.updateOlympiadStatuses();
        if (search != null && !search.isEmpty()) {
            model.addAttribute("olympiads", olympiadService.searchOlympiads(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("olympiads", olympiadService.findAll());
        }
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
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              Model model) {
        String error = validateEducationAndCourse(educationLevel, courseNumber, age);
        if (error != null) {
            model.addAttribute("olympiad", new Olympiad());
            model.addAttribute("error", error);
            return "admin/olympiad-form";
        }
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate);
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate);
        java.time.LocalDateTime now = java.time.LocalDateTime.now().withSecond(0).withNano(0);
        if (start.isBefore(now)) {
            Olympiad olympiad = new Olympiad();
            olympiad.setName(name);
            olympiad.setDescription(description);
            olympiad.setAge(age);
            olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
            olympiad.setCourseNumber(courseNumber);
            olympiad.setStartDate(start);
            olympiad.setEndDate(end);
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("error", "Дата начала не может быть в прошлом!");
            return "admin/olympiad-form";
        }
        if (end.isBefore(start)) {
            Olympiad olympiad = new Olympiad();
            olympiad.setName(name);
            olympiad.setDescription(description);
            olympiad.setAge(age);
            olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
            olympiad.setCourseNumber(courseNumber);
            olympiad.setStartDate(start);
            olympiad.setEndDate(end);
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("error", "Дата окончания не может быть раньше даты начала!");
            return "admin/olympiad-form";
        }
        Olympiad olympiad = new Olympiad();
        olympiad.setName(name);
        olympiad.setDescription(description);
        olympiad.setAge(age);
        olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
        olympiad.setCourseNumber(courseNumber);
        olympiad.setStartDate(start);
        olympiad.setEndDate(end);
        olympiad.setStatus(com.olimpiada.entity.OlympiadStatus.DRAFT);
        // --- Обработка фото ---
        if (image == null || image.isEmpty()) {
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("error", "Пожалуйста, добавьте фото олимпиады!");
            return "admin/olympiad-form";
        }
        try {
            String uploadDir = "olympiad_uploads";
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            image.transferTo(uploadPath.resolve(fileName));
            olympiad.setImagePath(fileName);
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при загрузке фото: " + e.getMessage());
            model.addAttribute("olympiad", olympiad);
            return "admin/olympiad-form";
        }
        // --- /Обработка фото ---
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
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               @RequestParam(value = "removeImage", required = false) String removeImage,
                               Model model) {
        Olympiad olympiad = olympiadService.findById(id);
        String error = validateEducationAndCourse(educationLevel, courseNumber, age);
        if (error != null) {
            olympiad.setName(name);
            olympiad.setDescription(description);
            olympiad.setAge(age);
            olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
            olympiad.setCourseNumber(courseNumber);
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate);
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate);
            olympiad.setStartDate(start);
            olympiad.setEndDate(end);
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("error", error);
            return "admin/olympiad-form";
        }
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate);
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate);
        java.time.LocalDateTime now = java.time.LocalDateTime.now().withSecond(0).withNano(0);
        if (start.isBefore(now)) {
            olympiad.setName(name);
            olympiad.setDescription(description);
            olympiad.setAge(age);
            olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
            olympiad.setCourseNumber(courseNumber);
            olympiad.setStartDate(start);
            olympiad.setEndDate(end);
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("error", "Дата начала не может быть в прошлом!");
            return "admin/olympiad-form";
        }
        if (end.isBefore(start)) {
            olympiad.setName(name);
            olympiad.setDescription(description);
            olympiad.setAge(age);
            olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
            olympiad.setCourseNumber(courseNumber);
            olympiad.setStartDate(start);
            olympiad.setEndDate(end);
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("error", "Дата окончания не может быть раньше даты начала!");
            return "admin/olympiad-form";
        }
        olympiad.setName(name);
        olympiad.setDescription(description);
        olympiad.setAge(age);
        olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
        olympiad.setCourseNumber(courseNumber);
        olympiad.setStartDate(start);
        olympiad.setEndDate(end);
        // --- Обработка фото ---
        boolean noOldImage = olympiad.getImagePath() == null || olympiad.getImagePath().isEmpty();
        boolean removeOld = "true".equals(removeImage);
        boolean noNewImage = image == null || image.isEmpty();
        if ((noOldImage || removeOld) && noNewImage) {
            olympiad.setName(name);
            olympiad.setDescription(description);
            olympiad.setAge(age);
            olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
            olympiad.setCourseNumber(courseNumber);
            olympiad.setStartDate(start);
            olympiad.setEndDate(end);
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("error", "Пожалуйста, добавьте фото олимпиады!");
            return "admin/olympiad-form";
        }
        try {
            if ("true".equals(removeImage)) {
                // Удалить старый файл, если был
                if (olympiad.getImagePath() != null) {
                    Path oldFile = Paths.get("olympiad_uploads").resolve(olympiad.getImagePath());
                    Files.deleteIfExists(oldFile);
                }
                olympiad.setImagePath(null);
            } else if (image != null && !image.isEmpty()) {
                // Заменить фото
                if (olympiad.getImagePath() != null) {
                    Path oldFile = Paths.get("olympiad_uploads").resolve(olympiad.getImagePath());
                    Files.deleteIfExists(oldFile);
                }
                String uploadDir = "olympiad_uploads";
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                image.transferTo(uploadPath.resolve(fileName));
                olympiad.setImagePath(fileName);
            }
        } catch (Exception e) {
            olympiad.setName(name);
            olympiad.setDescription(description);
            olympiad.setAge(age);
            olympiad.setEducationLevel(com.olimpiada.entity.EducationLevel.valueOf(educationLevel));
            olympiad.setCourseNumber(courseNumber);
            olympiad.setStartDate(start);
            olympiad.setEndDate(end);
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("error", "Ошибка при загрузке/удалении фото: " + e.getMessage());
            return "admin/olympiad-form";
        }
        // --- /Обработка фото ---
        olympiadService.save(olympiad);
        return "redirect:/admin/olympiads";
    }

    @PostMapping("/delete/{id}")
    public String deleteOlympiad(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Olympiad olympiad = olympiadService.findById(id);
        if (olympiad.getStatus() == com.olimpiada.entity.OlympiadStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("error", "Нельзя удалить активную олимпиаду!");
            return "redirect:/admin/olympiads";
        }
        olympiadService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Олимпиада успешно удалена");
        return "redirect:/admin/olympiads";
    }

    @GetMapping("/{id}")
    public String showOlympiadDetails(@PathVariable Long id, Model model) {
        return "redirect:/admin/tasks/olympiad/" + id;
    }

    @PostMapping("/publish/{id}")
    public String publishOlympiad(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Olympiad olympiad = olympiadService.findById(id);
        if (olympiad.getTasks() == null || olympiad.getTasks().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Нет ни одного задания в олимпиаде.  Добавьте задания для публикации олимпиады!");
            return "redirect:/admin/olympiads";
        }
        if (olympiad.getEndDate().isBefore(java.time.LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Олимпиада уже завершена и не может быть опубликована.");
            return "redirect:/admin/olympiads";
        }
        if (olympiad.getStatus() == com.olimpiada.entity.OlympiadStatus.PUBLISHED || olympiad.getStatus() == com.olimpiada.entity.OlympiadStatus.ACTIVE || olympiad.getStatus() == com.olimpiada.entity.OlympiadStatus.FINISHED) {
            return "redirect:/admin/olympiads";
        }
        olympiad.setStatus(com.olimpiada.entity.OlympiadStatus.PUBLISHED);
        olympiadService.save(olympiad);
        redirectAttributes.addFlashAttribute("success", String.format("Олимпиада '%s' успешно опубликована!", olympiad.getName()));
        return "redirect:/admin/olympiads";
    }

    private String validateEducationAndCourse(String educationLevel, Integer courseNumber, Integer age) {
        if (educationLevel.equals("SPO")) {
            if (age != null && age < 15) {
                return "Минимальный возраст участников для СПО — 15 лет";
            }
            if (courseNumber < 1 || courseNumber > 4) {
                return "Для СПО допустимы только курсы 1-4";
            }
        } else if (educationLevel.equals("BACHELOR")) {
            if (age != null && age < 17) {
                return "Минимальный возраст участников для бакалавриата — 17 лет";
            }
            if (courseNumber < 1 || courseNumber > 4) {
                return "Для бакалавриата допустимы только курсы 1-4";
            }
        } else if (educationLevel.equals("MASTER")) {
            if (age != null && age < 21) {
                return "Минимальный возраст участников для магистратуры — 21 год";
            }
            if (courseNumber < 1 || courseNumber > 2) {
                return "Для магистратуры допустимы только курсы 1-2";
            }
        }
        return null;
    }
} 