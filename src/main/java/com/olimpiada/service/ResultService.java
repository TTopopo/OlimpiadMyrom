package com.olimpiada.service;

import com.olimpiada.entity.Result;
import com.olimpiada.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {
    
    @Autowired
    private ResultRepository resultRepository;
    
    public List<Result> findByUserIdAndOlympiadId(Long userId, Long olympiadId) {
        return resultRepository.findByUserIdAndOlympiadId(userId, olympiadId);
    }
    
    public List<Result> findByUserId(Long userId) {
        return resultRepository.findByUserId(userId);
    }
    
    public List<Result> findByOlympiadId(Long olympiadId) {
        return resultRepository.findByOlympiadId(olympiadId);
    }
    
    public Result save(Result result) {
        return resultRepository.save(result);
    }
} 