package com.olimpiada.service;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.repository.OlympiadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OlympiadService {
    
    @Autowired
    private OlympiadRepository olympiadRepository;
    
    public List<Olympiad> findAll() {
        return olympiadRepository.findAll();
    }
    
    public Olympiad findById(Long id) {
        return olympiadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Olympiad not found with id: " + id));
    }
    
    public Olympiad save(Olympiad olympiad) {
        return olympiadRepository.save(olympiad);
    }
    
    public Olympiad update(Long id, Olympiad olympiad) {
        Olympiad existingOlympiad = findById(id);
        if (existingOlympiad != null) {
            olympiad.setId(id);
            return olympiadRepository.save(olympiad);
        }
        return null;
    }
    
    public void delete(Long id) {
        olympiadRepository.deleteById(id);
    }
    
    public void deleteById(Long id) {
        olympiadRepository.deleteById(id);
    }
    
    public List<Olympiad> findActiveOlympiads(LocalDateTime now) {
        return olympiadRepository.findByStartDateBeforeAndEndDateAfter(now, now);
    }
    
    public List<Olympiad> findUpcomingOlympiads(LocalDateTime now) {
        return olympiadRepository.findByStartDateAfter(now);
    }
    
    public List<Olympiad> findPastOlympiads(LocalDateTime now) {
        return olympiadRepository.findByEndDateBefore(now);
    }
} 