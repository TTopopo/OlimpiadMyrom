package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.Task;
import com.olimpiada.entity.TaskType;
import com.olimpiada.service.OlympiadService;
import com.olimpiada.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/tasks")
public class AdminTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private OlympiadService olympiadService;

    @GetMapping("/add/{olympiadId}")
    public String showAddTaskForm(@PathVariable Long olympiadId, Model model) {
        Olympiad olympiad = olympiadService.findById(olympiadId);
        Task task = new Task();
        task.setOlympiad(olympiad);
        
        model.addAttribute("task", task);
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("taskTypes", TaskType.values());
        return "admin/task-form";
    }

    @PostMapping("/add")
    public String addTask(@ModelAttribute Task task, @RequestParam Long olympiadId) {
        Olympiad olympiad = olympiadService.findById(olympiadId);
        task.setOlympiad(olympiad);
        taskService.save(task);
        return "redirect:/admin/olympiads/" + olympiadId;
    }

    @GetMapping("/edit/{id}")
    public String showEditTaskForm(@PathVariable Long id, Model model) {
        Task task = taskService.findById(id);
        model.addAttribute("task", task);
        model.addAttribute("taskTypes", TaskType.values());
        return "admin/task-form";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute Task task) {
        Task existingTask = taskService.findById(id);
        task.setOlympiad(existingTask.getOlympiad());
        taskService.save(task);
        return "redirect:/admin/olympiads/" + existingTask.getOlympiad().getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        Task task = taskService.findById(id);
        Long olympiadId = task.getOlympiad().getId();
        taskService.delete(id);
        return "redirect:/admin/olympiads/" + olympiadId;
    }
} 