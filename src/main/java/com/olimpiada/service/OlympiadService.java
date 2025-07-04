package com.olimpiada.service;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.repository.OlympiadRepository;
import com.olimpiada.repository.ParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.olimpiada.entity.Participation;
import com.olimpiada.entity.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OlympiadService {
    
    @Autowired
    private OlympiadRepository olympiadRepository;
    
    @Autowired
    private ParticipationRepository participationRepository;
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromEmail;
    
    public List<Olympiad> findAll() {
        return olympiadRepository.findAll();
    }
    
    public Olympiad findById(Long id) {
        return olympiadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
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
        return olympiadRepository.findByStatus(com.olimpiada.entity.OlympiadStatus.ACTIVE);
    }
    
    public List<Olympiad> findUpcomingOlympiads(LocalDateTime now) {
        return olympiadRepository.findByStatusAndStartDateAfter(com.olimpiada.entity.OlympiadStatus.PUBLISHED, now);
    }
    
    public List<Olympiad> findPastOlympiads(LocalDateTime now) {
        return olympiadRepository.findByStatus(com.olimpiada.entity.OlympiadStatus.FINISHED);
    }
    
    public List<Olympiad> searchOlympiads(String search) {
        return olympiadRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
    }
    
    public List<Olympiad> findActiveOlympiads() {
        return olympiadRepository.findByStatus(com.olimpiada.entity.OlympiadStatus.ACTIVE);
    }
    
    @Scheduled(cron = "0 * * * * *") // каждую минуту
    public void updateOlympiadStatuses() {
        List<Olympiad> olympiads = olympiadRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Olympiad olympiad : olympiads) {
            switch (olympiad.getStatus()) {
                case PUBLISHED:
                    if (olympiad.getStartDate().isBefore(now) || olympiad.getStartDate().isEqual(now)) {
                        olympiad.setStatus(com.olimpiada.entity.OlympiadStatus.ACTIVE);
                        olympiadRepository.save(olympiad);
                    }
                    break;
                case ACTIVE:
                    if (olympiad.getEndDate().isBefore(now) || olympiad.getEndDate().isEqual(now)) {
                        olympiad.setStatus(com.olimpiada.entity.OlympiadStatus.FINISHED);
                        olympiadRepository.save(olympiad);
                    }
                    break;
                default:
                    // ничего не делаем для DRAFT, FINISHED, CANCELLED
            }
        }
    }
    
    public boolean isUserRegisteredForOlympiad(Long userId, Long olympiadId) {
        return participationRepository.findByUserIdAndOlympiadId(userId, olympiadId).isPresent();
    }
    
    @Scheduled(cron = "0 0 * * * *") // каждый час
    public void sendOlympiadReminders() {
        List<Olympiad> upcoming = olympiadRepository.findByStatus(com.olimpiada.entity.OlympiadStatus.PUBLISHED);
        LocalDateTime now = LocalDateTime.now();
        for (Olympiad olympiad : upcoming) {
            long hoursToStart = java.time.Duration.between(now, olympiad.getStartDate()).toHours();
            if (hoursToStart == 24 || hoursToStart == 1) {
                List<Participation> participations = participationRepository.findByOlympiadId(olympiad.getId());
                for (Participation p : participations) {
                    User user = p.getUser();
                    if (mailSender != null) {
                        try {
                            MimeMessage message = mailSender.createMimeMessage();
                            MimeMessageHelper helper = new MimeMessageHelper(message, true);
                            helper.setTo(user.getEmail());
                            helper.setSubject(hoursToStart == 24 ? "Напоминание: олимпиада уже завтра!" : "Напоминание: олимпиада скоро начнётся!");
                            helper.setText("Здравствуйте, " + user.getFullName() + "!\n\nНапоминаем, что олимпиада '" + olympiad.getName() + "' начнётся " + olympiad.getStartDate().toString() + ".\n\nЖелаем удачи!\n\nС уважением, команда Олимпиад.", false);
                            helper.setFrom(fromEmail);
                            mailSender.send(message);
                        } catch (Exception e) { /* log error */ }
                    }
                }
            }
        }
    }
} 