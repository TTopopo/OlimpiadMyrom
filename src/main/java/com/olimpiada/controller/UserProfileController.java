package com.olimpiada.controller;

import com.olimpiada.entity.User;
import com.olimpiada.entity.UserAnswer;
import com.olimpiada.entity.UserRole;
import com.olimpiada.service.UserAnswerService;
import com.olimpiada.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAnswerService userAnswerService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        
        if (user.getRole() == UserRole.ADMIN) {
            // Для администратора показываем админ-профиль
            return "admin/profile";
        } else {
            // Для обычного пользователя показываем обычный профиль
            List<UserAnswer> userAnswers = userAnswerService.findByUserId(user.getId());
            model.addAttribute("userAnswers", userAnswers);
            return "user/profile";
        }
    }
} 