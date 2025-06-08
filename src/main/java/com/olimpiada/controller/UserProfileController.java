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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import com.olimpiada.entity.Participation;
import com.olimpiada.repository.ParticipationRepository;
import com.olimpiada.entity.Result;
import com.olimpiada.service.ResultService;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAnswerService userAnswerService;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private ResultService resultService;

    // DTO для истории участия
    class ParticipationProfileDTO {
        private Participation participation;
        private Float totalScore;
        private Float maxPossibleScore;
        private Integer place;
        private String certificatePath;
        private String gramotaPath;
        private boolean showDiploma;
        private Result result;
        // геттеры/сеттеры
        public Participation getParticipation() { return participation; }
        public void setParticipation(Participation participation) { this.participation = participation; }
        public Float getTotalScore() { return totalScore; }
        public void setTotalScore(Float totalScore) { this.totalScore = totalScore; }
        public Float getMaxPossibleScore() { return maxPossibleScore; }
        public void setMaxPossibleScore(Float maxPossibleScore) { this.maxPossibleScore = maxPossibleScore; }
        public Integer getPlace() { return place; }
        public void setPlace(Integer place) { this.place = place; }
        public String getCertificatePath() { return certificatePath; }
        public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }
        public String getGramotaPath() { return gramotaPath; }
        public void setGramotaPath(String gramotaPath) { this.gramotaPath = gramotaPath; }
        public boolean isShowDiploma() { return showDiploma; }
        public void setShowDiploma(boolean showDiploma) { this.showDiploma = showDiploma; }
        public Result getResult() { return result; }
        public void setResult(Result result) { this.result = result; }
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        
        if (user.getRole() == UserRole.ADMIN) {
            // Для администратора показываем админ-профиль
            return "admin/profile";
        } else {
            List<Participation> participations = participationRepository.findByUserId(user.getId());
            List<ParticipationProfileDTO> participationDtos = new java.util.ArrayList<>();
            for (Participation p : participations) {
                ParticipationProfileDTO dto = new ParticipationProfileDTO();
                dto.setParticipation(p);
                List<Result> results = resultService.findByUserIdAndOlympiadId(user.getId(), p.getOlympiad().getId());
                if (!results.isEmpty()) {
                    Result r = results.get(0);
                    dto.setResult(r);
                    dto.setTotalScore(r.getTotalScore());
                    dto.setMaxPossibleScore(r.getMaxPossibleScore());
                    boolean finished = p.getOlympiad().getStatus() != null && p.getOlympiad().getStatus().name().equals("FINISHED");
                    if (finished) {
                        dto.setPlace(r.getPlace());
                    } else {
                        dto.setPlace(null);
                    }
                    dto.setCertificatePath("/user/olympiad/" + p.getOlympiad().getId() + "/certificate/pdf");
                    boolean isPrize = r.getPlace() != null && r.getPlace() >= 1 && r.getPlace() <= 3;
                    dto.setShowDiploma(finished && isPrize);
                    if (dto.isShowDiploma()) {
                        dto.setGramotaPath("/user/olympiad/" + p.getOlympiad().getId() + "/diploma/pdf");
                    } else {
                        dto.setGramotaPath(null);
                    }
                }
                participationDtos.add(dto);
            }
            model.addAttribute("participationDtos", participationDtos);
            return "user/profile";
        }
    }

    @GetMapping("/profile/edit-nickname")
    public String editNicknameForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        return "user/edit-nickname";
    }

    @PostMapping("/profile/edit-nickname")
    public ResponseEntity<?> updateNickname(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String nickname, HttpServletRequest request, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Никнейм не может быть пустым!");
        }
        userService.updateNickname(user.getId(), nickname.trim());
        // Обновляем Principal в сессии
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                new com.olimpiada.security.CustomUserDetails(userService.findByEmail(user.getEmail())),
                user.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()))
            )
        );
        // Если AJAX — просто 200 OK
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return ResponseEntity.ok().build();
        }
        // Если обычная форма — редирект
        return ResponseEntity.status(302).header("Location", "/user/profile").build();
    }
} 