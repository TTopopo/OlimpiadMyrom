package com.olimpiada.config;

import com.olimpiada.entity.User;
import com.olimpiada.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class AdminInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin")) {
            User admin = new User();
            admin.setFullName("Администратор");
            admin.setEmail("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setBirthDate(LocalDate.of(2000, 1, 1));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }
} 