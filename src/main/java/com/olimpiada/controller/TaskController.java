package com.olimpiada.controller;

import com.olimpiada.entity.Task;
import com.olimpiada.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @GetMapping
    public String listTasks(Model model) {
        model.addAttribute("tasks", taskService.findAll());
        return "tasks/list";
    }
    
    @GetMapping("/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        Task task = taskService.findById(id);
        if (task != null) {
            model.addAttribute("task", task);
            return "tasks/view";
        }
        return "redirect:/tasks";
    }
    
    @GetMapping("/olympiad/{olympiadId}")
    public String listTasksByOlympiad(@PathVariable Long olympiadId, Model model) {
        model.addAttribute("tasks", taskService.findByOlympiad(olympiadId));
        return "tasks/list";
    }
    
    @PostMapping
    public String createTask(@ModelAttribute Task task) {
        taskService.save(task);
        return "redirect:/tasks";
    }
    
    @PutMapping("/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute Task task) {
        taskService.update(id, task);
        return "redirect:/tasks/{id}";
    }
    
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return "redirect:/tasks";
    }
} 