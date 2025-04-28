package com.olimpiada.service;

import com.olimpiada.entity.Task;
import com.olimpiada.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    public List<Task> findAll() {
        return taskRepository.findAll();
    }
    
    public Task findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }
    
    public Task save(Task task) {
        return taskRepository.save(task);
    }
    
    public Task update(Long id, Task task) {
        Task existingTask = findById(id);
        if (existingTask != null) {
            task.setId(id);
            return taskRepository.save(task);
        }
        return null;
    }
    
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
    
    public List<Task> findByOlympiad(Long olympiadId) {
        return taskRepository.findByOlympiadId(olympiadId);
    }
} 