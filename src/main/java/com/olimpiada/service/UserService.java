package com.olimpiada.service;

import com.olimpiada.entity.User;
import com.olimpiada.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public User update(Long id, User user) {
        User existingUser = findById(id);
        if (existingUser != null) {
            user.setId(id);
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                user.setPassword(existingUser.getPassword());
            }
            return userRepository.save(user);
        }
        return null;
    }
    
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
    
    public List<User> searchUsers(String search) {
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);
    }
    
    public void updateNickname(Long id, String nickname) {
        User user = findById(id);
        user.setNickname(nickname);
        userRepository.save(user);
    }
} 