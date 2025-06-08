package com.olimpiada.service;

import com.olimpiada.entity.*;
import com.olimpiada.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;

@Service
public class CertificateService {
    
    @Autowired
    private ResultRepository resultRepository;
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8084}")
    private String baseUrl;
    
    public enum CertificateType {
        PARTICIPANT("Сертификат участника"),
        WINNER("Диплом победителя (1 место)"),
        RUNNER_UP_2("Диплом призера (2 место)"),
        RUNNER_UP_3("Диплом призера (3 место)");
        
        private final String title;
        
        CertificateType(String title) {
            this.title = title;
        }
        
        public String getTitle() {
            return title;
        }
    }
    
    public CertificateType determineCertificateType(Result result) {
        if (result == null || result.getPlace() == null) {
            return CertificateType.PARTICIPANT;
        }
        
        int place = result.getPlace();
        
        switch (place) {
            case 1:
                return CertificateType.WINNER;
            case 2:
                return CertificateType.RUNNER_UP_2;
            case 3:
                return CertificateType.RUNNER_UP_3;
            default:
                return CertificateType.PARTICIPANT;
        }
    }
    
    public boolean isEligibleForCertificate(Result result) {
        if (result == null) {
            return false;
        }
        
        // Сертификат выдается всем, кто завершил олимпиаду
        return result.getStatus() == ResultStatus.COMPLETED;
    }
    
    public boolean isEligibleForDiploma(Result result) {
        if (result == null || result.getPlace() == null) {
            return false;
        }
        
        // Грамота выдается только за 1-3 места
        return result.getPlace() <= 3;
    }
    
    public String generateCertificateNumber(User user, Olympiad olympiad, CertificateType type) {
        String prefix = type == CertificateType.PARTICIPANT ? "CERT" : "DIPL";
        return String.format("%s-%d-%d-%s", 
            prefix,
            user.getId(),
            olympiad.getId(),
            java.time.LocalDate.now().getYear());
    }
    
    public void sendCertificateEmail(User user, Olympiad olympiad) {
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(user.getEmail());
                helper.setSubject("Ваш сертификат участника олимпиады");
                String link = baseUrl + "/user/olympiad/" + olympiad.getId() + "/certificate/pdf";
                helper.setText("Здравствуйте, " + user.getFullName() + "!\n\nПоздравляем с успешным завершением олимпиады '" + olympiad.getName() + "'!\nВаш сертификат доступен по ссылке: " + link + "\n\nС уважением, команда Олимпиад.", false);
                helper.setFrom(fromEmail);
                mailSender.send(message);
            } catch (Exception e) { /* log error */ }
        }
    }
    
    public void sendDiplomaEmail(User user, Olympiad olympiad) {
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(user.getEmail());
                helper.setSubject("Ваша грамота за олимпиаду");
                String link = baseUrl + "/user/olympiad/" + olympiad.getId() + "/diploma/pdf";
                helper.setText("Здравствуйте, " + user.getFullName() + "!\n\nПоздравляем с призовым местом в олимпиаде '" + olympiad.getName() + "'!\nВаша грамота доступна по ссылке: " + link + "\n\nС уважением, команда Олимпиад.", false);
                helper.setFrom(fromEmail);
                mailSender.send(message);
            } catch (Exception e) { /* log error */ }
        }
    }
} 