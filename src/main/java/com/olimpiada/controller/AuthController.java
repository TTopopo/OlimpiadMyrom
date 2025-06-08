package com.olimpiada.controller;

import com.olimpiada.entity.EducationLevel;
import com.olimpiada.entity.User;
import com.olimpiada.entity.UserRole;
import com.olimpiada.repository.UserRepository;
import com.olimpiada.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Optional;
import java.net.URLEncoder;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                          @RequestParam String email,
                          @RequestParam String birthDate,
                          @RequestParam String educationLevel,
                          @RequestParam Integer courseNumber,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          Model model) {
        String error = validateRegistration(fullName, email, birthDate, educationLevel, courseNumber, password, confirmPassword);
        if (error != null) {
            model.addAttribute("error", error);
            return "register";
        }

        if (userRepository.existsByEmail(email)) {
            try {
                String errorMsg = URLEncoder.encode("Email уже используется", "UTF-8");
                return "redirect:/register?error=" + errorMsg;
            } catch (Exception e) {
                return "redirect:/register?error=email";
            }
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setBirthDate(LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE));
        user.setEducationLevel(EducationLevel.valueOf(educationLevel));
        user.setCourseNumber(courseNumber);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        // nickname по email до @
        String nickname = email.contains("@") ? email.substring(0, email.indexOf("@")) : fullName.split(" ")[0];
        user.setNickname(nickname);
        userRepository.save(user);

        // Автоматический вход после регистрации
        com.olimpiada.security.CustomUserDetails customUserDetails = new com.olimpiada.security.CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        return "redirect:/login";
    }

    private String validateRegistration(String fullName, String email, String birthDate, String educationLevel, Integer courseNumber, String password, String confirmPassword) {
        if (fullName == null || fullName.trim().isEmpty()) return "ФИО обязательно";
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) return "Введите корректный email";
        if (birthDate == null || birthDate.trim().isEmpty()) return "Дата рождения обязательна";
        try {
            LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            return "Неверный формат даты";
        }
        if (educationLevel == null || (!educationLevel.equals("BACHELOR") && !educationLevel.equals("MASTER") && !educationLevel.equals("SPO"))) return "Выберите уровень образования";
        if (educationLevel.equals("BACHELOR") && (courseNumber < 1 || courseNumber > 4)) return "Для бакалавриата допустимы только курсы 1-4";
        if (educationLevel.equals("MASTER") && (courseNumber < 1 || courseNumber > 2)) return "Для магистратуры допустимы только курсы 1-2";
        if (educationLevel.equals("SPO") && (courseNumber < 1 || courseNumber > 4)) return "Для СПО допустимы только курсы 1-4";
        if (password == null || password.length() < 6) return "Пароль должен быть не менее 6 символов";
        if (!password.equals(confirmPassword)) return "Пароли не совпадают";
        return null;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверный email или пароль");
        }
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Пользователь с таким email не найден");
            return "forgot-password";
        }
        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        String resetLink = "http://localhost:8084/reset-password?token=" + token;
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(user.getEmail());
                helper.setSubject("Восстановление пароля");
                helper.setText("Для сброса пароля перейдите по ссылке: <a href='" + resetLink + "'>Сбросить пароль</a>", true);
                mailSender.send(message);
                model.addAttribute("message", "Письмо с инструкцией отправлено на ваш email");
            } catch (Exception e) {
                model.addAttribute("error", "Ошибка отправки письма: " + e.getMessage());
                model.addAttribute("resetLink", resetLink);
                return "forgot-password";
            }
        } else {
            model.addAttribute("message", "Письмо не отправлено (SMTP не настроен), но вы можете воспользоваться ссылкой ниже для сброса пароля.");
            model.addAttribute("resetLink", resetLink);
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        Optional<User> userOpt = userRepository.findByResetPasswordToken(token);
        if (userOpt.isEmpty() || userOpt.get().getResetPasswordTokenExpiry() == null || userOpt.get().getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Ссылка для сброса пароля недействительна или устарела");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, @RequestParam String password, @RequestParam String confirmPassword, Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            model.addAttribute("token", token);
            return "reset-password";
        }
        Optional<User> userOpt = userRepository.findByResetPasswordToken(token);
        if (userOpt.isEmpty() || userOpt.get().getResetPasswordTokenExpiry() == null || userOpt.get().getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Ссылка для сброса пароля недействительна или устарела");
            return "reset-password";
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
        model.addAttribute("message", "Пароль успешно изменён. Теперь вы можете войти.");
        return "reset-password";
    }

    @GetMapping("/api/check-email")
    @ResponseBody
    public java.util.Map<String, Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return java.util.Collections.singletonMap("exists", exists);
    }
} 