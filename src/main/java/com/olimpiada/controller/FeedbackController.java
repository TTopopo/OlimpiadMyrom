package com.olimpiada.controller;

import com.olimpiada.entity.FeedbackMessage;
import com.olimpiada.repository.FeedbackMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    @Autowired
    private FeedbackMessageRepository feedbackMessageRepository;
    @Autowired(required = false)
    private JavaMailSender mailSender;
    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromEmail;

    @PostMapping
    public void sendFeedback(@RequestBody FeedbackMessage feedback) {
        feedback.setCreatedAt(java.time.LocalDateTime.now());
        feedbackMessageRepository.save(feedback);
        // Здесь можно добавить отправку email админу
    }

    @GetMapping("/all")
    public List<FeedbackMessage> getAllFeedback() {
        return feedbackMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/reply")
    public void replyFeedback(@RequestParam Long id, @RequestParam String reply) {
        FeedbackMessage msg = feedbackMessageRepository.findById(id).orElse(null);
        if (msg != null && msg.getAdminReply() == null) {
            msg.setAdminReply(reply);
            msg.setAdminReplyAt(java.time.LocalDateTime.now());
            feedbackMessageRepository.save(msg);
            // Отправка письма пользователю
            if (mailSender != null) {
                try {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true);
                    helper.setTo(msg.getEmail());
                    helper.setSubject("Ответ на ваш вопрос на платформе олимпиад");
                    StringBuilder text = new StringBuilder();
                    text.append("Здравствуйте, ")
                        .append(msg.getName() != null ? msg.getName() : "пользователь").append("!\n\n");
                    text.append("Вы задали вопрос на платформе Олимпиад:\n");
                    if (msg.getTopic() != null) text.append("Тема: ").append(msg.getTopic()).append("\n");
                    text.append("Вопрос: ").append(msg.getMessage()).append("\n\n");
                    text.append("Ответ администратора:\n").append(reply).append("\n\n");
                    text.append("Если у вас остались вопросы — пишите нам на support@olimpiada.ru или отвечайте на это письмо.\n\n");
                    text.append("С уважением, команда Олимпиад.\n");
                    text.append("https://olimpiada.ru\n");
                    helper.setText(text.toString(), false);
                    helper.setFrom(fromEmail);
                    mailSender.send(message);
                } catch (Exception e) {
                    // Можно залогировать ошибку
                }
            }
        }
    }

    @GetMapping("/user")
    public List<FeedbackMessage> getUserFeedback(@RequestParam(required = false) String email, java.security.Principal principal) {
        String userEmail = email;
        if (principal != null) userEmail = principal.getName();
        if (userEmail == null) return List.of();
        return feedbackMessageRepository.findAllByEmailOrderByCreatedAtDesc(userEmail);
    }

    @PostMapping("/send-email")
    public void sendEmail(@RequestBody EmailRequest req) throws MessagingException {
        if (mailSender == null) return;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(req.getEmail());
        helper.setSubject("Ответ на ваш вопрос на платформе олимпиад");
        helper.setText(req.getReply(), false);
        helper.setFrom(fromEmail);
        mailSender.send(message);
    }

    public static class EmailRequest {
        private String email;
        private String reply;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
    }
} 