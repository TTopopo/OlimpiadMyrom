package com.olimpiada.controller;

import com.olimpiada.entity.CourseType;
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

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
            return "redirect:/register?error=Email уже используется";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setBirthDate(LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE));
        user.setEducationLevel(EducationLevel.valueOf(educationLevel));
        user.setCourseNumber(courseNumber);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);

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
        if (educationLevel == null || (!educationLevel.equals("BACHELOR") && !educationLevel.equals("MASTER"))) return "Выберите уровень образования";
        if (educationLevel.equals("BACHELOR") && (courseNumber < 1 || courseNumber > 4)) return "Для бакалавриата допустимы только курсы 1-4";
        if (educationLevel.equals("MASTER") && (courseNumber < 1 || courseNumber > 2)) return "Для магистратуры допустимы только курсы 1-2";
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
} 