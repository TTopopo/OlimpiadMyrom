package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.Task;
import com.olimpiada.entity.User;
import com.olimpiada.entity.UserAnswer;
import com.olimpiada.service.OlympiadService;
import com.olimpiada.service.TaskService;
import com.olimpiada.service.UserAnswerService;
import com.olimpiada.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.olimpiada.entity.Result;
import com.olimpiada.service.ResultService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletResponse;
import com.lowagie.text.pdf.BaseFont;

@Controller
@RequestMapping("/user/olympiad")
@PreAuthorize("hasRole('USER')")
public class UserOlympiadController {

    @Autowired
    private OlympiadService olympiadService;
    @Autowired
    private ResultService resultService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserAnswerService userAnswerService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public String listOlympiads(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("activeOlympiads", olympiadService.findActiveOlympiads(now));
        model.addAttribute("upcomingOlympiads", olympiadService.findUpcomingOlympiads(now));
        model.addAttribute("pastOlympiads", olympiadService.findPastOlympiads(now));
        return "olympiads/list";
    }

    @GetMapping("/{id}")
    public String viewOlympiad(@PathVariable Long id, Model model, Authentication authentication) {
        Olympiad olympiad = olympiadService.findById(id);
        if (olympiad.getEndDate().isBefore(LocalDateTime.now())) {
            return "redirect:/user/olympiads";
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Task> tasks = taskService.getTasksByOlympiadId(id);
        List<UserAnswer> userAnswers = userAnswerService.findByUserAndOlympiad(user.getId(), id);
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("tasks", tasks);
        model.addAttribute("userAnswers", userAnswers);
        return "user/olympiad/view";
    }

    @PostMapping("/{olympiadId}/task/{taskId}/answer")
    public String submitAnswer(@PathVariable Long olympiadId, 
                             @PathVariable Long taskId,
                             @RequestParam String answer,
                             Authentication authentication) {
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return "redirect:/user/olympiad/" + olympiadId;
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(user);
        userAnswer.setTask(task);
        userAnswer.setAnswer(answer);
        userAnswerService.save(userAnswer);
        return "redirect:/user/olympiad/" + olympiadId;
    }

    @PostMapping("/{olympiadId}/task/{taskId}/answer/ajax")
    @ResponseBody
    public String submitAnswerAjax(@PathVariable Long olympiadId,
                                   @PathVariable Long taskId,
                                   @RequestParam String answer,
                                   Authentication authentication) {
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return "error";
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<UserAnswer> existing = userAnswerService.findByUserAndOlympiad(user.getId(), olympiadId);
        UserAnswer userAnswer = existing.stream().filter(ua -> ua.getTask().getId().equals(taskId)).findFirst().orElse(null);
        if (userAnswer == null) {
            userAnswer = new UserAnswer();
            userAnswer.setUser(user);
            userAnswer.setTask(task);
        }
        userAnswer.setAnswer(answer);
        userAnswer.setSubmissionTime(java.time.LocalDateTime.now());
        userAnswerService.save(userAnswer);
        return "ok";
    }

    @GetMapping("/results")
    public String getOlympiadResults(@RequestParam Long olympiadId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        List<Result> results = resultService.findByUserIdAndOlympiadId(user.getId(), olympiadId);
        model.addAttribute("results", results);
        return "results/user-list";
    }

    @GetMapping("/{id}/active")
    public String activeOlympiad(@PathVariable Long id, Model model, Authentication authentication, @RequestParam(value = "task", required = false) Integer taskIdx) {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Task> tasks = taskService.getTasksByOlympiadId(id);
        List<UserAnswer> userAnswers = userAnswerService.findByUserAndOlympiad(user.getId(), id);
        
        // Формируем optionsList для каждого задания
        for (Task t : tasks) {
            if (("SINGLE_CHOICE".equals(t.getTaskType()) || "MULTIPLE_CHOICE".equals(t.getTaskType())) && t.getOptions() != null) {
                t.setOptionsList(java.util.Arrays.asList(t.getOptions().split(";")));
            }
        }
        int currentTaskIdx = (taskIdx != null && taskIdx >= 0 && taskIdx < tasks.size()) ? taskIdx : 0;
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("tasks", tasks);
        model.addAttribute("user", user);
        model.addAttribute("currentTaskIdx", currentTaskIdx);
        model.addAttribute("userAnswers", userAnswers != null ? userAnswers : new ArrayList<>());
        return "user/olympiad/active";
    }

    @GetMapping("/{id}/start")
    public String startOlympiad(@PathVariable Long id, Model model, Authentication authentication, @RequestParam(required = false) String error) {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        boolean isRegistered = olympiadService.isUserRegisteredForOlympiad(user.getId(), id);
        if (!isRegistered) {
            return "redirect:/api/participations/olympiad/" + id + "/confirm";
        }
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("user", user);
        model.addAttribute("error", error);
        return "user/olympiad/start";
    }

    @GetMapping("/{id}/certificate")
    public String certificatePage(@PathVariable Long id, Model model, Authentication authentication) {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("user", user);
        return "user/olympiad/certificate";
    }

    @GetMapping("/{id}/certificate/pdf")
    public void downloadCertificatePdf(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response, Authentication authentication) throws java.io.IOException {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=certificate.pdf");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();
            // Подключаем кириллический шрифт (arial.ttf должен быть в resources/fonts/)
            BaseFont bf = BaseFont.createFont("fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle = new Font(bf, 32, Font.BOLD);
            Font fontText = new Font(bf, 20, Font.NORMAL);
            Font fontSmall = new Font(bf, 14, Font.NORMAL);
            // Заголовок
            Paragraph title = new Paragraph("СЕРТИФИКАТ УЧАСТНИКА", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            // ФИО и олимпиада
            document.add(new Paragraph("Настоящим подтверждается, что", fontText));
            document.add(new Paragraph(user.getFullName(), fontTitle));
            document.add(new Paragraph("успешно прошёл(ла) олимпиаду:", fontText));
            document.add(new Paragraph(olympiad.getName(), fontText));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Дата: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), fontSmall));
            document.add(new Paragraph("Уникальный номер: " + user.getId() + "-" + olympiad.getId() + "-" + java.time.LocalDate.now().getYear(), fontSmall));
            document.add(new Paragraph(" "));
            // Печать и подпись (можно добавить картинку, если есть)
            document.add(new Paragraph(" "));
            document.add(new Paragraph("_____________________", fontSmall));
            document.add(new Paragraph("Подпись организатора", fontSmall));
            document.close();
            response.getOutputStream().write(baos.toByteArray());
        } catch (Exception e) {
            response.getOutputStream().write("Ошибка генерации сертификата".getBytes(StandardCharsets.UTF_8));
        }
    }
} 