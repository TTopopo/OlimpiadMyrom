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
    
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задание не найдено"));
    }
    
    public List<Task> getTasksByOlympiadId(Long olympiadId) {
        return taskRepository.findByOlympiadId(olympiadId);
    }
    
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }
    
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
    
    public List<Task> searchTasksByOlympiad(Long olympiadId, String search) {
        return taskRepository.findByOlympiadIdAndTitleContainingIgnoreCaseOrOlympiadIdAndTaskTextContainingIgnoreCase(olympiadId, search, olympiadId, search);
    }
} 