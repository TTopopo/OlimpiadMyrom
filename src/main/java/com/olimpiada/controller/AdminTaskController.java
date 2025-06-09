package com.olimpiada.controller;

import com.olimpiada.entity.Task;
import com.olimpiada.entity.TaskType;
import com.olimpiada.entity.Olympiad;
import com.olimpiada.service.TaskService;
import com.olimpiada.service.OlympiadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/tasks")
public class AdminTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private OlympiadService olympiadService;

    @GetMapping("/add")
    public String showAddForm(@RequestParam Long olympiadId, Model model) {
        Task task = new Task();
        Olympiad olympiad = olympiadService.findById(olympiadId);
        task.setOlympiad(olympiad);
        task.setTaskType(TaskType.SINGLE_CHOICE);
        
        model.addAttribute("task", task);
        model.addAttribute("taskTypes", Arrays.asList(TaskType.values()));
        model.addAttribute("options", java.util.List.of("", "")); // минимум два пустых варианта
        model.addAttribute("correctAnswers", java.util.List.of()); // пустой список правильных ответов
        return "admin/task-form";
    }

    @PostMapping("/add")
    public String addTask(@ModelAttribute Task task,
                         @RequestParam Long olympiadId,
                         @RequestParam(required = false) List<String> answers,
                         @RequestParam(required = false) Integer correctAnswer,
                         @RequestParam(required = false) List<Integer> correctAnswers,
                         @RequestParam(required = false) String correctTextAnswer,
                         @RequestParam(required = false) String correctCodeAnswer,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String error = validateTask(task, answers, correctAnswer, correctAnswers, correctTextAnswer, correctCodeAnswer);
        if (error != null) {
            Olympiad olympiad = olympiadService.findById(olympiadId);
            task.setOlympiad(olympiad);
            model.addAttribute("task", task);
            model.addAttribute("taskTypes", Arrays.asList(TaskType.values()));
            model.addAttribute("error", error);
            return "admin/task-form";
        }
        try {
            Olympiad olympiad = olympiadService.findById(olympiadId);
            task.setOlympiad(olympiad);
            // Сохраняем все варианты в options
            if (answers != null && !answers.isEmpty()) {
                List<String> filteredAnswers = answers.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
                task.setOptions(String.join(";", filteredAnswers));
            }
            // Устанавливаем правильные ответы в зависимости от типа задания
            switch (task.getTaskType()) {
                case SINGLE_CHOICE:
                    if (correctAnswer != null && answers != null && correctAnswer < answers.size()) {
                        task.setCorrectAnswer(String.valueOf(correctAnswer));
                    }
                    break;
                case MULTIPLE_CHOICE:
                    if (correctAnswers != null && answers != null) {
                        StringBuilder correctAnswersStr = new StringBuilder();
                        for (Integer index : correctAnswers) {
                            if (index < answers.size()) {
                                if (correctAnswersStr.length() > 0) {
                                    correctAnswersStr.append(";");
                                }
                                correctAnswersStr.append(index);
                            }
                        }
                        task.setCorrectAnswer(correctAnswersStr.toString());
                    }
                    break;
                case TEXT_ANSWER:
                    task.setCorrectAnswer(correctTextAnswer);
                    break;
                case CODE_ANSWER:
                    task.setCorrectAnswer(correctCodeAnswer);
                    break;
            }
            // --- Обработка фото ---
            if (image != null && !image.isEmpty()) {
                String uploadDir = "uploads";
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                image.transferTo(uploadPath.resolve(fileName));
                task.setImagePath(fileName);
            }
            // --- /Обработка фото ---
            taskService.saveTask(task);
            redirectAttributes.addFlashAttribute("success", "Задание успешно добавлено");
            return "redirect:/admin/olympiads/" + task.getOlympiad().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении задания: " + e.getMessage());
            return "redirect:/admin/tasks/add?olympiadId=" + olympiadId;
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("taskTypes", Arrays.asList(TaskType.values()));
        List<String> options = new java.util.ArrayList<>();
        List<String> correctAnswers = new java.util.ArrayList<>();
        if (task.getTaskType() == TaskType.SINGLE_CHOICE || task.getTaskType() == TaskType.MULTIPLE_CHOICE) {
            if (task.getOptions() != null && !task.getOptions().isEmpty()) {
                options = java.util.Arrays.asList(task.getOptions().split(";"));
            }
            if (task.getCorrectAnswer() != null && !task.getCorrectAnswer().isEmpty()) {
                correctAnswers = java.util.Arrays.asList(task.getCorrectAnswer().split(";"));
            }
        }
        model.addAttribute("options", options);
        model.addAttribute("correctAnswers", correctAnswers);
        // Для текстового и кодового ответа
        if (task.getTaskType() == TaskType.TEXT_ANSWER) {
            model.addAttribute("correctTextAnswer", task.getCorrectAnswer());
        }
        if (task.getTaskType() == TaskType.CODE_ANSWER) {
            model.addAttribute("correctCodeAnswer", task.getCorrectAnswer());
        }
        return "admin/task-form";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Long id,
                           @ModelAttribute Task task,
                           @RequestParam(required = false) List<String> answers,
                           @RequestParam(required = false) Integer correctAnswer,
                           @RequestParam(required = false) List<Integer> correctAnswers,
                           @RequestParam(required = false) String correctTextAnswer,
                           @RequestParam(required = false) String correctCodeAnswer,
                           @RequestParam(value = "image", required = false) MultipartFile image,
                           @RequestParam(value = "removeImage", required = false) String removeImage,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String error = validateTask(task, answers, correctAnswer, correctAnswers, correctTextAnswer, correctCodeAnswer);
        if (error != null) {
            Task existingTask = taskService.getTaskById(id);
            task.setOlympiad(existingTask.getOlympiad());
            model.addAttribute("task", task);
            model.addAttribute("taskTypes", Arrays.asList(TaskType.values()));
            model.addAttribute("error", error);
            return "admin/task-form";
        }
        try {
            Task existingTask = taskService.getTaskById(id);
            existingTask.setTitle(task.getTitle());
            existingTask.setTaskText(task.getTaskText());
            existingTask.setMaxScore(task.getMaxScore());
            existingTask.setTaskType(task.getTaskType());
            // Сохраняем все варианты в options
            if (answers != null && !answers.isEmpty()) {
                List<String> filteredAnswers = answers.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
                existingTask.setOptions(String.join(";", filteredAnswers));
            }
            // Устанавливаем правильные ответы в зависимости от типа задания
            switch (task.getTaskType()) {
                case SINGLE_CHOICE:
                    if (correctAnswer != null && answers != null && correctAnswer < answers.size()) {
                        existingTask.setCorrectAnswer(String.valueOf(correctAnswer));
                    }
                    break;
                case MULTIPLE_CHOICE:
                    if (correctAnswers != null && answers != null) {
                        StringBuilder correctAnswersStr = new StringBuilder();
                        for (Integer index : correctAnswers) {
                            if (index < answers.size()) {
                                if (correctAnswersStr.length() > 0) {
                                    correctAnswersStr.append(";");
                                }
                                correctAnswersStr.append(index);
                            }
                        }
                        existingTask.setCorrectAnswer(correctAnswersStr.toString());
                    }
                    break;
                case TEXT_ANSWER:
                    existingTask.setCorrectAnswer(correctTextAnswer);
                    break;
                case CODE_ANSWER:
                    existingTask.setCorrectAnswer(correctCodeAnswer);
                    break;
            }
            // --- Обработка фото ---
            if (image != null && !image.isEmpty()) {
                String uploadDir = "uploads";
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                image.transferTo(uploadPath.resolve(fileName));
                existingTask.setImagePath(fileName);
            } else {
                // Если не выбрано новое фото, оставить старое
                existingTask.setImagePath(existingTask.getImagePath());
            }
            // --- /Обработка фото ---
            taskService.saveTask(existingTask);
            redirectAttributes.addFlashAttribute("success", "Задание успешно обновлено");
            return "redirect:/admin/olympiads/" + existingTask.getOlympiad().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении задания: " + e.getMessage());
            return "redirect:/admin/tasks/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Task task = taskService.getTaskById(id);
            Long olympiadId = task.getOlympiad().getId();
            taskService.deleteTask(id);
            redirectAttributes.addFlashAttribute("success", "Задание успешно удалено");
            return "redirect:/admin/tasks/olympiad/" + olympiadId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении задания: " + e.getMessage());
            return "redirect:/admin/olympiads/" + id;
        }
    }

    @GetMapping("/olympiad/{olympiadId}")
    public String listTasksByOlympiad(@PathVariable Long olympiadId, @RequestParam(value = "search", required = false) String search, Model model) {
        List<Task> tasks;
        if (search != null && !search.isEmpty()) {
            tasks = taskService.searchTasksByOlympiad(olympiadId, search);
            model.addAttribute("search", search);
        } else {
            tasks = taskService.getTasksByOlympiadId(olympiadId);
        }
        // Вычисляем displayCorrectAnswer для каждого задания
        for (Task task : tasks) {
            if ((task.getTaskType() == TaskType.SINGLE_CHOICE || task.getTaskType() == TaskType.MULTIPLE_CHOICE) && task.getOptions() != null) {
                String[] opts = task.getOptions().split(";");
                if (task.getTaskType() == TaskType.SINGLE_CHOICE) {
                    try {
                        int idx = Integer.parseInt(task.getCorrectAnswer());
                        if (idx >= 0 && idx < opts.length) {
                            task.setDisplayCorrectAnswer(opts[idx]);
                        } else {
                            task.setDisplayCorrectAnswer(task.getCorrectAnswer());
                        }
                    } catch (Exception e) {
                        task.setDisplayCorrectAnswer(task.getCorrectAnswer());
                    }
                } else { // MULTIPLE_CHOICE
                    String[] idxs = task.getCorrectAnswer() != null ? task.getCorrectAnswer().split(";") : new String[0];
                    List<String> correctOpts = new java.util.ArrayList<>();
                    for (String idxStr : idxs) {
                        try {
                            int idx = Integer.parseInt(idxStr);
                            if (idx >= 0 && idx < opts.length) {
                                correctOpts.add(opts[idx]);
                            }
                        } catch (Exception ignored) {}
                    }
                    task.setDisplayCorrectAnswer(String.join(", ", correctOpts));
                }
            } else {
                task.setDisplayCorrectAnswer(task.getCorrectAnswer());
            }
        }
        Olympiad olympiad = olympiadService.findById(olympiadId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("olympiadId", olympiadId);
        return "admin/task-list";
    }

    private String validateTask(Task task, List<String> answers, Integer correctAnswer, List<Integer> correctAnswers, String correctTextAnswer, String correctCodeAnswer) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) return "Название задания обязательно";
        if (task.getMaxScore() == null || task.getMaxScore() <= 0) return "Максимальный балл должен быть больше 0";
        if (task.getTaskType() == null) return "Тип задания обязателен";
        switch (task.getTaskType()) {
            case SINGLE_CHOICE:
                if (answers == null || answers.size() < 2) return "Добавьте минимум два варианта ответа";
                if (correctAnswer == null || correctAnswer < 0 || correctAnswer >= answers.size()) return "Выберите правильный ответ";
                break;
            case MULTIPLE_CHOICE:
                if (answers == null || answers.size() < 2) return "Добавьте минимум два варианта ответа";
                if (correctAnswers == null || correctAnswers.isEmpty()) return "Выберите хотя бы один правильный ответ";
                break;
            case TEXT_ANSWER:
                if (correctTextAnswer == null || correctTextAnswer.trim().isEmpty()) return "Введите правильный текстовый ответ";
                break;
            case CODE_ANSWER:
                if (correctCodeAnswer == null || correctCodeAnswer.trim().isEmpty()) return "Введите правильный код-ответ";
                break;
        }
        return null;
    }
} 