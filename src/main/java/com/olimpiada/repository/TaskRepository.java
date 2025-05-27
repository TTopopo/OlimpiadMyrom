package com.olimpiada.repository;

import com.olimpiada.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOlympiadId(Long olympiadId);
    List<Task> findByOlympiadIdAndTitleContainingIgnoreCaseOrOlympiadIdAndTaskTextContainingIgnoreCase(Long olympiadId1, String title, Long olympiadId2, String taskText);
} 