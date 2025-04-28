package com.olimpiada.controller;

import com.olimpiada.entity.User;
import com.olimpiada.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Collections;

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/api/auth/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String birthDate,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "register";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "register";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setBirthDate(java.time.LocalDate.parse(birthDate));
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");

        userRepository.save(user);

        // Автоматический вход после регистрации
        com.olimpiada.security.CustomUserDetails customUserDetails = new com.olimpiada.security.CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверный email или пароль");
        }
        return "login";
    }
} 