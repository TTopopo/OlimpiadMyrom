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
        
        model.addAttribute("task", task);
        model.addAttribute("taskTypes", Arrays.asList(TaskType.values()));
        return "admin/task-form";
    }

    @PostMapping("/add")
    public String addTask(@ModelAttribute Task task,
                         @RequestParam(required = false) List<String> answers,
                         @RequestParam(required = false) Integer correctAnswer,
                         @RequestParam(required = false) List<Integer> correctAnswers,
                         @RequestParam(required = false) String correctTextAnswer,
                         @RequestParam(required = false) String correctCodeAnswer,
                         RedirectAttributes redirectAttributes) {
        try {
            // Устанавливаем правильные ответы в зависимости от типа задания
            switch (task.getTaskType()) {
                case SINGLE_CHOICE:
                    if (correctAnswer != null && answers != null && correctAnswer < answers.size()) {
                        task.setCorrectAnswer(answers.get(correctAnswer));
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
                                correctAnswersStr.append(answers.get(index));
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

            taskService.saveTask(task);
            redirectAttributes.addFlashAttribute("success", "Задание успешно добавлено");
            return "redirect:/admin/olympiads/" + task.getOlympiad().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении задания: " + e.getMessage());
            return "redirect:/admin/tasks/add?olympiadId=" + task.getOlympiad().getId();
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("taskTypes", Arrays.asList(TaskType.values()));
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
                           RedirectAttributes redirectAttributes) {
        try {
            Task existingTask = taskService.getTaskById(id);
            existingTask.setTitle(task.getTitle());
            existingTask.setTaskText(task.getTaskText());
            existingTask.setMaxScore(task.getMaxScore());
            existingTask.setTaskType(task.getTaskType());

            // Устанавливаем правильные ответы в зависимости от типа задания
            switch (task.getTaskType()) {
                case SINGLE_CHOICE:
                    if (correctAnswer != null && answers != null && correctAnswer < answers.size()) {
                        existingTask.setCorrectAnswer(answers.get(correctAnswer));
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
                                correctAnswersStr.append(answers.get(index));
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
            return "redirect:/admin/olympiads/" + olympiadId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении задания: " + e.getMessage());
            return "redirect:/admin/olympiads/" + id;
        }
    }

    @GetMapping("/olympiad/{olympiadId}")
    public String listTasksByOlympiad(@PathVariable Long olympiadId, Model model) {
        List<Task> tasks = taskService.getTasksByOlympiadId(olympiadId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("olympiadId", olympiadId);
        return "admin/task-list";
    }
} 