package com.olimpiada.repository;

import com.olimpiada.entity.FeedbackMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackMessageRepository extends JpaRepository<FeedbackMessage, Long> {
    java.util.List<FeedbackMessage> findAllByOrderByCreatedAtDesc();
    java.util.List<FeedbackMessage> findAllByEmailOrderByCreatedAtDesc(String email);
} 