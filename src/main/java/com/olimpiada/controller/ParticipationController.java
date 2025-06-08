package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.Participation;
import com.olimpiada.entity.User;
import com.olimpiada.repository.OlympiadRepository;
import com.olimpiada.repository.ParticipationRepository;
import com.olimpiada.repository.UserRepository;
import com.olimpiada.repository.UserAnswerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Controller
@RequestMapping("/api/participations")
public class ParticipationController {
    private static final Logger logger = LoggerFactory.getLogger(ParticipationController.class);
    private final ParticipationRepository participationRepository;
    private final OlympiadRepository olympiadRepository;
    private final UserRepository userRepository;
    @Autowired
    private UserAnswerRepository userAnswerRepository;
    @Autowired(required = false)
    private JavaMailSender mailSender;
    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromEmail;

    @Autowired
    public ParticipationController(
            ParticipationRepository participationRepository,
            OlympiadRepository olympiadRepository,
            UserRepository userRepository) {
        this.participationRepository = participationRepository;
        this.olympiadRepository = olympiadRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/olympiad/{olympiadId}")
    public String registerForOlympiad(@PathVariable Long olympiadId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            logger.warn("[REGISTER] Попытка регистрации администратором");
            return "redirect:/olympiads?error=admin_forbidden";
        }
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        boolean alreadyRegistered = participationRepository.findByUserIdAndOlympiadId(user.getId(), olympiadId).isPresent();
        boolean educationMatch = user.getEducationLevel() != null && olympiad.getEducationLevel() != null
            && user.getEducationLevel().name().equals(olympiad.getEducationLevel().name());
        boolean courseMatch = user.getCourseNumber() != null && olympiad.getCourseNumber() != null
            && user.getCourseNumber().intValue() == olympiad.getCourseNumber().intValue();
        boolean olympiadActive = olympiad.getStatus() != null && olympiad.getStatus().name().equals("ACTIVE");
        boolean olympiadUpcoming = olympiad.getStatus() != null && olympiad.getStatus().name().equals("PUBLISHED");
        boolean hasAnswers = false;
        try {
            hasAnswers = userAnswerRepository.findByUserIdAndTaskOlympiadId(user.getId(), olympiadId)
                .stream().anyMatch(ua -> ua.getAnswer() != null && !ua.getAnswer().trim().isEmpty());
        } catch (Exception e) {}
        if (alreadyRegistered) {
            return "redirect:/olympiads?error=already_registered";
        }
        if (!educationMatch || !courseMatch) {
            logger.warn("[REGISTER] Не подходит по курсу или уровню: user={}, olympiad={}, educationMatch={}, courseMatch={}", email, olympiad.getName(), educationMatch, courseMatch);
            return "redirect:/olympiads?error=not_allowed";
        }
        Participation participation = new Participation();
        participation.setUser(user);
        participation.setOlympiad(olympiad);
        participation.setRegistrationDate(java.time.LocalDateTime.now());
        participationRepository.save(participation);
        logger.info("[REGISTER] Успешно зарегистрирован: user={}, olympiad={}", email, olympiad.getName());
        // Отправка email пользователю
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(user.getEmail());
                helper.setSubject("Вы успешно записались на олимпиаду!");
                helper.setText("Здравствуйте, " + user.getFullName() + "!\n\nВы успешно записались на олимпиаду: '" + olympiad.getName() + "'.\nЖдём вас!\n\nДата начала: " + olympiad.getStartDate().toString() + "\n\nС уважением, команда Олимпиад.", false);
                helper.setFrom(fromEmail);
                mailSender.send(message);
            } catch (Exception e) {
                logger.error("Ошибка отправки email при регистрации на олимпиаду", e);
            }
        }
        if (olympiadActive) {
            return "redirect:/user/olympiad/" + olympiadId + "/start";
        }
        if (olympiadUpcoming) {
            return "redirect:/olympiads?success=registered";
        }
        return "redirect:/olympiads";
    }

    @GetMapping("/user")
    public String getUserParticipations(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Participation> participations = participationRepository.findByUserId(user.getId());
        // Для каждой participation определяем, есть ли ответы
        for (Participation p : participations) {
            boolean hasAnswers = userAnswerRepository.findByUserIdAndTaskOlympiadId(user.getId(), p.getOlympiad().getId())
                .stream().anyMatch(ua -> ua.getAnswer() != null && !ua.getAnswer().trim().isEmpty());
            p.setHasAnswers(hasAnswers);
        }
        model.addAttribute("participations", participations);
        return "participations/list";
    }

    @GetMapping("/olympiad/{olympiadId}/participants")
    @PreAuthorize("hasRole('ADMIN')")
    public String getOlympiadParticipants(@PathVariable Long olympiadId, Model model) {
        List<Participation> participations = participationRepository.findByOlympiadId(olympiadId);
        model.addAttribute("participations", participations);
        model.addAttribute("olympiadId", olympiadId);
        return "participations/participants";
    }

    @GetMapping("/olympiad/{olympiadId}/confirm")
    public String confirmParticipation(
        @PathVariable Long olympiadId,
        @RequestParam(required = false) String error,
        @RequestParam(required = false) String success,
        Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            logger.warn("Попытка доступа к подтверждению участия администратором или неавторизованным пользователем");
            return "redirect:/olympiads?error=admin_forbidden";
        }
        Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> new RuntimeException("Олимпиада не найдена"));
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        boolean alreadyRegistered = participationRepository.findByUserIdAndOlympiadId(user.getId(), olympiadId).isPresent();
        boolean olympiadActive = olympiad.getStatus() != null && olympiad.getStatus().name().equals("ACTIVE");
        boolean olympiadUpcoming = olympiad.getStatus() != null && olympiad.getStatus().name().equals("PUBLISHED");
        boolean educationMatch = user.getEducationLevel() != null && olympiad.getEducationLevel() != null
            && user.getEducationLevel().name().equals(olympiad.getEducationLevel().name());
        boolean courseMatch = user.getCourseNumber() != null && olympiad.getCourseNumber() != null
            && user.getCourseNumber().intValue() == olympiad.getCourseNumber().intValue();
        boolean hasAnswers = false;
        try {
            hasAnswers = userAnswerRepository.findByUserIdAndTaskOlympiadId(user.getId(), olympiadId)
                .stream().anyMatch(ua -> ua.getAnswer() != null && !ua.getAnswer().trim().isEmpty());
        } catch (Exception e) {}
        if (alreadyRegistered) {
            if (!hasAnswers) {
                if (olympiadUpcoming) {
                    model.addAttribute("olympiad", olympiad);
                    model.addAttribute("alreadyRegistered", true);
                    return "participations/confirm";
                }
                if (olympiadActive) {
                    return "redirect:/user/olympiad/" + olympiadId + "/start";
                }
            } else {
                return "redirect:/olympiads?error=already_participated";
            }
        }
        if (!educationMatch || !courseMatch) {
            return "redirect:/olympiads?error=not_allowed";
        }
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("error", error);
        model.addAttribute("success", success);
        return "participations/confirm";
    }
} 