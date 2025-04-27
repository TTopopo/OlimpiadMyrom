package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.Task;
import com.olimpiada.repository.OlympiadRepository;
import com.olimpiada.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskRepository taskRepository;
    private final OlympiadRepository olympiadRepository;

    @Autowired
    public TaskController(TaskRepository taskRepository, OlympiadRepository olympiadRepository) {
        this.taskRepository = taskRepository;
        this.olympiadRepository = olympiadRepository;
    }

    @GetMapping("/olympiad/{olympiadId}")
    public String getTasksByOlympiad(@PathVariable Long olympiadId, Model model) {
        List<Task> tasks = taskRepository.findByOlympiadId(olympiadId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("olympiadId", olympiadId);
        return "tasks/list";
    }

    @GetMapping("/olympiad/{olympiadId}/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createTaskForm(@PathVariable Long olympiadId, Model model) {
        Task task = new Task();
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        task.setOlympiad(olympiad);
        model.addAttribute("task", task);
        return "tasks/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createTask(@ModelAttribute Task task) {
        taskRepository.save(task);
        return "redirect:/api/tasks/olympiad/" + task.getOlympiad().getId();
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editTaskForm(@PathVariable Long id, Model model) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Задание не найдено"));
        model.addAttribute("task", task);
        return "tasks/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateTask(@PathVariable Long id, @ModelAttribute Task updatedTask) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Задание не найдено"));
        
        task.setTaskText(updatedTask.getTaskText());
        task.setTaskType(updatedTask.getTaskType());
        task.setMaxScore(updatedTask.getMaxScore());
        task.setTimeLimit(updatedTask.getTimeLimit());
        
        taskRepository.save(task);
        return "redirect:/api/tasks/olympiad/" + task.getOlympiad().getId();
    }

    @GetMapping("/{id}")
    public String getTask(@PathVariable Long id, Model model) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Задание не найдено"));
        model.addAttribute("task", task);
        return "tasks/view";
    }
} 