package com.olimpiada.repository;

import com.olimpiada.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByUserId(Long userId);
    List<Result> findByOlympiadId(Long olympiadId);
    List<Result> findByUserIdAndOlympiadId(Long userId, Long olympiadId);
} 