package com.olimpiada.controller;

import com.lowagie.text.pdf.*;
import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.Task;
import com.olimpiada.entity.User;
import com.olimpiada.entity.UserAnswer;
import com.olimpiada.service.OlympiadService;
import com.olimpiada.service.TaskService;
import com.olimpiada.service.UserAnswerService;
import com.olimpiada.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.olimpiada.entity.Result;
import com.olimpiada.service.ResultService;
import com.lowagie.text.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletResponse;
import com.olimpiada.entity.Participation;
import com.olimpiada.repository.ParticipationRepository;
import java.util.HashMap;
import java.util.Map;
import com.olimpiada.entity.OlympiadStatus;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.lowagie.text.Image;
import com.olimpiada.service.CertificateService;
import java.io.IOException;

@Controller
@RequestMapping("/user/olympiad")
@PreAuthorize("hasRole('USER')")
public class UserOlympiadController {

    @Autowired
    private OlympiadService olympiadService;
    @Autowired
    private ResultService resultService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserAnswerService userAnswerService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private CertificateService certificateService;

    @GetMapping
    public String listOlympiads(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("activeOlympiads", olympiadService.findActiveOlympiads(now));
        model.addAttribute("upcomingOlympiads", olympiadService.findUpcomingOlympiads(now));
        model.addAttribute("pastOlympiads", olympiadService.findPastOlympiads(now));
        return "olympiads/list";
    }

    @GetMapping("/{id}")
    public String viewOlympiad(@PathVariable Long id, Model model, Authentication authentication) {
        Olympiad olympiad = olympiadService.findById(id);
        if (olympiad.getEndDate().isBefore(LocalDateTime.now())) {
            return "redirect:/user/olympiads";
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Task> tasks = taskService.getTasksByOlympiadId(id);
        List<UserAnswer> userAnswers = userAnswerService.findByUserAndOlympiad(user.getId(), id);
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("tasks", tasks);
        model.addAttribute("userAnswers", userAnswers);
        model.addAttribute("user", user);
        return "user/olympiad/view";
    }

    @PostMapping("/{olympiadId}/task/{taskId}/answer")
    public String submitAnswer(@PathVariable Long olympiadId, 
                             @PathVariable Long taskId,
                             @RequestParam String answer,
                             Authentication authentication) {
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return "redirect:/user/olympiad/" + olympiadId;
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(user);
        userAnswer.setTask(task);
        userAnswer.setAnswer(answer);
        userAnswerService.save(userAnswer);
        return "redirect:/user/olympiad/" + olympiadId;
    }

    @PostMapping("/{olympiadId}/task/{taskId}/answer/ajax")
    @ResponseBody
    public String submitAnswerAjax(@PathVariable Long olympiadId,
                                   @PathVariable Long taskId,
                                   @RequestParam String answer,
                                   Authentication authentication) {
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return "error";
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<UserAnswer> existing = userAnswerService.findByUserAndOlympiad(user.getId(), olympiadId);
        UserAnswer userAnswer = existing.stream().filter(ua -> ua.getTask().getId().equals(taskId)).findFirst().orElse(null);
        if (userAnswer == null) {
            userAnswer = new UserAnswer();
            userAnswer.setUser(user);
            userAnswer.setTask(task);
        }
        userAnswer.setAnswer(answer);
        userAnswer.setSubmissionTime(java.time.LocalDateTime.now());
        userAnswerService.save(userAnswer);
        return "ok";
    }

    @GetMapping("/results")
    public String getOlympiadResults(@RequestParam Long olympiadId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        List<Result> results = resultService.findByUserIdAndOlympiadId(user.getId(), olympiadId);
        model.addAttribute("results", results);
        return "results/user-list";
    }

    @GetMapping("/{id}/active")
    public String activeOlympiad(@PathVariable Long id, Model model, Authentication authentication, @RequestParam(value = "task", required = false) Integer taskIdx) {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Task> tasks = taskService.getTasksByOlympiadId(id);
        List<UserAnswer> userAnswersList = userAnswerService.findByUserAndOlympiad(user.getId(), id);
        
        Map<Long, UserAnswer> userAnswers = new HashMap<>();
        for (UserAnswer ua : userAnswersList) {
            userAnswers.put(ua.getTask().getId(), ua);
        }
        // Получаем Participation
        Participation participation = participationRepository.findByUserIdAndOlympiadId(user.getId(), olympiad.getId()).orElse(null);
        System.out.println("[DEBUG] userId=" + user.getId() + ", olympiadId=" + olympiad.getId());
        System.out.println("[DEBUG] Participation found: " + participation);
        if (participation != null) {
            System.out.println("[DEBUG] Participation startTime: " + participation.getStartTime());
            if (participation.isFinished()) {
                return "redirect:/user/olympiads?error=time_expired";
            }
        }
        if (participation == null) {
            participation = new Participation();
            participation.setUser(user);
            participation.setOlympiad(olympiad);
            participation.setStartTime(java.time.LocalDateTime.now());
            participation.setRegistrationDate(java.time.LocalDateTime.now());
            participationRepository.save(participation);
            System.out.println("[DEBUG] Created new participation with startTime: " + participation.getStartTime());
        }
        // Вычисляем оставшееся время (60 минут минус прошедшее с startTime)
        int totalSeconds = 60 * 60;
        int remainingSeconds = totalSeconds;
        if (participation.getStartTime() != null) {
            java.time.Duration duration = java.time.Duration.between(participation.getStartTime(), java.time.LocalDateTime.now());
            remainingSeconds = totalSeconds - (int) duration.getSeconds();
            if (remainingSeconds < 0) remainingSeconds = 0;
            System.out.println("[DEBUG] Timer calculation: now=" + java.time.LocalDateTime.now() + ", startTime=" + participation.getStartTime() + ", remainingSeconds=" + remainingSeconds);
        }
        for (Task t : tasks) {
            if (("SINGLE_CHOICE".equals(t.getTaskType()) || "MULTIPLE_CHOICE".equals(t.getTaskType())) && t.getOptions() != null) {
                t.setOptionsList(java.util.Arrays.asList(t.getOptions().split(";")));
            }
        }
        int currentTaskIdx = (taskIdx != null && taskIdx >= 0 && taskIdx < tasks.size()) ? taskIdx : 0;
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("tasks", tasks);
        model.addAttribute("user", user);
        model.addAttribute("currentTaskIdx", currentTaskIdx);
        model.addAttribute("userAnswers", userAnswers);
        model.addAttribute("remainingSeconds", remainingSeconds);
        model.addAttribute("participation", participation);
        return "user/olympiad/active";
    }

    @GetMapping("/{id}/start")
    public String startOlympiad(@PathVariable Long id, Model model, Authentication authentication, HttpServletResponse response) {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        // Запретить кэширование стартовой страницы
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Проверяем статус олимпиады
        if (olympiad.getStatus() == OlympiadStatus.PUBLISHED) {
            // Олимпиада ещё не началась — отправляем на подтверждение участия
            return "redirect:/api/participations/olympiad/" + id + "/confirm";
        }
        if (olympiad.getStatus() != OlympiadStatus.ACTIVE) {
            // Завершена или другой статус — ошибка
            return "redirect:/olympiads?error=olympiad_not_active";
        }

        Participation participation = participationRepository.findByUserIdAndOlympiadId(user.getId(), id).orElse(null);
        // Новая проверка: если уже завершил олимпиаду, не даём повторно стартовать
        if (participation != null && participation.isFinished()) {
            return "redirect:/olympiads?error=already_participated";
        }
        List<UserAnswer> userAnswers = userAnswerService.findByUserAndOlympiad(user.getId(), id);
        boolean hasAnyAnswer = userAnswers.stream().anyMatch(ua -> ua.getAnswer() != null && !ua.getAnswer().isEmpty());

        if (hasAnyAnswer) {
            return "redirect:/olympiads?error=already_participated";
        }

        // Если есть участие, но нет startTime - значит пользователь только зарегистрировался
        if (participation != null && participation.getStartTime() == null) {
            model.addAttribute("olympiad", olympiad);
            model.addAttribute("user", user);
            model.addAttribute("userAnswers", userAnswers);
            return "user/olympiad/start";
        }

        // Если есть участие и есть startTime - значит олимпиада уже начата
        if (participation != null && participation.getStartTime() != null) {
            return "redirect:/user/olympiad/" + id + "/active";
        }

        // Если нет участия - нужно сначала зарегистрироваться
        return "redirect:/api/participations/olympiad/" + id + "/confirm";
    }

    @PostMapping("/{id}/do-start")
    public String doStartOlympiad(@PathVariable Long id, Authentication authentication) {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        Participation participation = participationRepository.findByUserIdAndOlympiadId(user.getId(), olympiad.getId()).orElse(null);
        if (participation == null) {
            participation = new Participation();
            participation.setUser(user);
            participation.setOlympiad(olympiad);
            participation.setRegistrationDate(java.time.LocalDateTime.now());
        }
        participation.setStartTime(java.time.LocalDateTime.now());
        participationRepository.save(participation);

        // Удаляем все старые ответы пользователя для этой олимпиады
        userAnswerService.deleteAllByUserAndOlympiad(user.getId(), id);

        return "redirect:/user/olympiad/" + id + "/active";
    }

    @GetMapping("/{id}/certificate")
    public String certificatePage(@PathVariable Long id, Model model, Authentication authentication) {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        
        // Получаем результат пользователя
        List<Result> results = resultService.findByUserIdAndOlympiadId(user.getId(), id);
        if (results.isEmpty()) {
            return "redirect:/user/olympiad";
        }
        
        Result result = results.get(0);
        if (!certificateService.isEligibleForCertificate(result)) {
            model.addAttribute("error", "К сожалению, вы не завершили олимпиаду.");
            return "user/olympiad/certificate";
        }
        // Всегда показываем только сертификат участника
        CertificateService.CertificateType certType = CertificateService.CertificateType.PARTICIPANT;
        String certNumber = certificateService.generateCertificateNumber(user, olympiad, certType);
        
        model.addAttribute("olympiad", olympiad);
        model.addAttribute("user", user);
        model.addAttribute("result", result);
        model.addAttribute("certificateType", certType);
        model.addAttribute("certificateNumber", certNumber);
        model.addAttribute("isDiploma", false);
        return "user/olympiad/certificate";
    }

    @GetMapping("/{id}/certificate/pdf")
    public void downloadCertificatePdf(@PathVariable Long id, HttpServletResponse response, Authentication authentication) throws IOException {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        // Получаем результат пользователя
        List<Result> results = resultService.findByUserIdAndOlympiadId(user.getId(), id);
        if (results.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Результат не найден");
            return;
        }
        Result result = results.get(0);
        if (!certificateService.isEligibleForCertificate(result)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Олимпиада не завершена");
            return;
        }
        // Отправляем email с сертификатом (ссылкой)
        certificateService.sendCertificateEmail(user, olympiad);
        // Всегда только сертификат участника
        String certNumber = String.format("CERT-%d-%d-%d", user.getId(), olympiad.getId(), java.time.LocalDate.now().getYear());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=certificate.pdf");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 60, 60, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            BaseFont bf;
            try {
                bf = BaseFont.createFont("fonts/ofont.ru_Roboto.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception fontEx) {
                bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            }
            Font fontTitle = new Font(bf, 48, Font.BOLD, new java.awt.Color(37,99,235));
            Font fontText = new Font(bf, 26, Font.NORMAL, new java.awt.Color(30,41,59));
            Font fontName = new Font(bf, 32, Font.BOLD, new java.awt.Color(255, 180, 0));
            Font fontSmall = new Font(bf, 18, Font.NORMAL, new java.awt.Color(30,41,59));
            float pageWidth = document.getPageSize().getWidth();
            float pageHeight = document.getPageSize().getHeight();
            // Фон и рамка
            PdfContentByte bg = writer.getDirectContentUnder();
            bg.setColorFill(new java.awt.Color(230, 245, 255));
            bg.rectangle(0, 0, pageWidth, pageHeight);
            bg.fill();
            bg.setColorStroke(new java.awt.Color(37,99,235));
            bg.setLineWidth(4f);
            bg.roundRectangle(40, 40, pageWidth-80, pageHeight-80, 30);
            bg.stroke();
            // Медалька (PNG) — крупная, с белой подложкой, левый верхний угол
            float medalSize = 160f;
            float medalX = 80f;
            float medalY = pageHeight - medalSize - 80f;
            // Подложка (белый круг с тенью)
            PdfContentByte medalBg = writer.getDirectContentUnder();
            medalBg.setColorFill(new java.awt.Color(255,255,255,230));
            medalBg.circle(medalX + medalSize/2, medalY + medalSize/2, medalSize/2 + 16);
            medalBg.fill();
            medalBg.setColorFill(new java.awt.Color(200, 200, 200, 60));
            medalBg.circle(medalX + medalSize/2 + 8, medalY + medalSize/2 - 8, medalSize/2 + 22);
            medalBg.fill();
            try {
                java.net.URL medalUrl = getClass().getClassLoader().getResource("medal.png");
                if (medalUrl != null) {
                    Image medalImg = Image.getInstance(medalUrl);
                    medalImg.scaleAbsolute(medalSize, medalSize);
                    medalImg.setAbsolutePosition(medalX, medalY);
                    document.add(medalImg);
                }
            } catch (Exception e) { e.printStackTrace(); }
            // Основной блок текста — справа от медальки
            float textBlockX = 260;
            float textBlockWidth = pageWidth - textBlockX - 80;
            float textBlockTop = pageHeight-120;
            PdfPTable table = new PdfPTable(1);
            table.setTotalWidth(textBlockWidth);
            table.setLockedWidth(true);
            table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(new Phrase("СЕРТИФИКАТ", fontTitle));
            table.addCell(new Phrase("\nНастоящий сертификат подтверждает, что", fontText));
            table.addCell(new Phrase(user.getFullName(), fontName));
            table.addCell(new Phrase("принял(а) участие в олимпиаде:", fontText));
            table.addCell(new Phrase(olympiad.getName(), new Font(bf, 24, Font.BOLD, new java.awt.Color(37,99,235))));
            table.writeSelectedRows(0, -1, textBlockX, textBlockTop, writer.getDirectContent());
            // Даты и уникальный номер — почти в самом низу
            float infoY = 110; // почти внизу страницы
            float infoX = textBlockX;
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("Дата выдачи: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), fontSmall), infoX, infoY+60, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("Дата получения: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), fontSmall), infoX, infoY+30, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("Уникальный номер: " + certNumber, fontSmall), infoX, infoY, 0);
            // QR-код
            String qrText = "https://olimpiada.ru/certificate/" + user.getId() + "/" + olympiad.getId();
            int qrSize = 80;
            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, qrSize, qrSize);
                BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
                ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
                ImageIO.write(qrImage, "PNG", qrBaos);
                Image qrPdfImg = Image.getInstance(qrBaos.toByteArray());
                qrPdfImg.scaleAbsolute(qrSize, qrSize);
                qrPdfImg.setAbsolutePosition(60, 60);
                document.add(qrPdfImg);
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("Проверить подлинность", new Font(bf, 12, Font.ITALIC, new java.awt.Color(37,99,235))), 60, 50, 0);
            } catch (Exception e) {}
            // Подпись и печать — смещаю ниже
            PdfContentByte stamp = writer.getDirectContent();
            float stampX = pageWidth-180;
            float stampY = 140f;
            stamp.setLineWidth(2.2f);
            stamp.setColorStroke(new java.awt.Color(37,99,235));
            stamp.circle(stampX, stampY, 52);
            stamp.stroke();
            stamp.beginText();
            stamp.setFontAndSize(bf, 16);
            stamp.setColorFill(new java.awt.Color(37,99,235));
            stamp.showTextAligned(Element.ALIGN_CENTER, "Олимпиада", stampX, stampY+14, 0);
            stamp.setFontAndSize(bf, 14);
            stamp.showTextAligned(Element.ALIGN_CENTER, "МИ ВлГУ", stampX, stampY-6, 0);
            stamp.endText();
            // Генерация случайной подписи
            String[] signatures = {"Иванов И.И.", "Петров П.П.", "Сидоров А.А.", "Кузнецов В.В.", "Смирнов Д.Д."};
            String randomSignature = signatures[(int)(Math.random()*signatures.length)];
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("_______________________", fontSmall), pageWidth-120, 210, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase(randomSignature, new Font(bf, 18, Font.ITALIC, new java.awt.Color(30,41,59))), pageWidth-120, 190, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Оргкомитет Олимпиады", fontSmall), pageWidth-120, 170, 0);
            document.close();
            response.getOutputStream().write(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            response.getOutputStream().write("Ошибка генерации документа".getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/{id}/diploma/pdf")
    public void downloadDiplomaPdf(@PathVariable Long id, HttpServletResponse response, Authentication authentication) throws IOException {
        Olympiad olympiad = olympiadService.findById(id);
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        // Получаем результат пользователя
        List<Result> results = resultService.findByUserIdAndOlympiadId(user.getId(), id);
        if (results.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Результат не найден");
            return;
        }
        Result result = results.get(0);
        // Только для 1, 2, 3 места
        if (result.getPlace() == null || result.getPlace() > 3) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Грамота доступна только призёрам");
            return;
        }
        // Отправляем email с грамотой (ссылкой)
        certificateService.sendDiplomaEmail(user, olympiad);
        CertificateService.CertificateType certType = certificateService.determineCertificateType(result);
        String certNumber = certificateService.generateCertificateNumber(user, olympiad, certType);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=diploma.pdf");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 60, 60, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            BaseFont bf;
            try {
                bf = BaseFont.createFont("fonts/ofont.ru_Roboto.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception fontEx) {
                bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            }
            // Цвета по месту
            java.awt.Color mainColor = new java.awt.Color(255, 215, 0); // золото
            java.awt.Color bgColor = new java.awt.Color(255, 250, 220); // светло-золотой
            if (result.getPlace() == 2) {
                mainColor = new java.awt.Color(180, 180, 180); // серебро
                bgColor = new java.awt.Color(245, 245, 245); // светло-серый
            } else if (result.getPlace() == 3) {
                mainColor = new java.awt.Color(205, 127, 50); // бронза
                bgColor = new java.awt.Color(255, 240, 220); // светло-бронзовый
            }
            Font fontTitle = new Font(bf, 52, Font.BOLD, new java.awt.Color(37,99,235));
            Font fontText = new Font(bf, 28, Font.NORMAL, new java.awt.Color(30,41,59));
            Font fontSmall = new Font(bf, 18, Font.NORMAL, new java.awt.Color(30,41,59));
            Font fontNameSmall = new Font(bf, 30, Font.BOLD, new java.awt.Color(37,99,235)); // синий
            float pageWidth = document.getPageSize().getWidth();
            float pageHeight = document.getPageSize().getHeight();
            // Фон и рамка по месту
            PdfContentByte bg = writer.getDirectContentUnder();
            bg.setColorFill(bgColor);
            bg.rectangle(0, 0, pageWidth, pageHeight);
            bg.fill();
            bg.setColorStroke(mainColor);
            bg.setLineWidth(5f);
            bg.roundRectangle(40, 40, pageWidth-80, pageHeight-80, 30);
            bg.stroke();
            // --- Заголовок по центру ---
            float centerX = pageWidth / 2;
            float textBlockX = 120;
            float textBlockTop = pageHeight-110;
            float textLineStep = 44f; // чуть больше
            float curY = textBlockTop;
            Font fontTitleSmall = new Font(bf, 44, Font.BOLD, new java.awt.Color(37,99,235)); // больше
            Font fontTextSmall = new Font(bf, 24, Font.NORMAL, new java.awt.Color(30,41,59)); // больше
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("ГРАМОТА", fontTitleSmall), centerX, curY, 0);
            curY -= textLineStep + 8;
            // --- Медаль в правом верхнем углу с белым кругом и тенью ---
            float medalSize = 200f;
            float medalX = pageWidth-250;
            float medalY = pageHeight-260;
            // Более компактный круглый фон строго под медалью, смещён ниже
            PdfContentByte medalBg = writer.getDirectContentUnder();
            medalBg.setColorFill(new java.awt.Color(255,255,255,235));
            medalBg.circle(medalX + medalSize/2 - 10, medalY + medalSize/2, 105); // круг чуть левее
            medalBg.fill();
            // Лёгкая серая тень
            medalBg.setColorFill(new java.awt.Color(200, 200, 200, 60));
            medalBg.circle(medalX + medalSize/2 - 4, medalY + medalSize/2 - 6, 115); // тень тоже левее
            medalBg.fill();
            // Медаль
            String medalFile = "medal_gold.png";
            if (result.getPlace() == 2) medalFile = "medal_silver.png";
            else if (result.getPlace() == 3) medalFile = "medal_bronze.png";
            try {
                java.net.URL medalUrl = getClass().getClassLoader().getResource(medalFile);
                if (medalUrl != null) {
                    Image medalImg = Image.getInstance(medalUrl);
                    medalImg.scaleAbsolute(medalSize, medalSize);
                    medalImg.setAbsolutePosition(medalX, medalY);
                    document.add(medalImg);
                }
            } catch (Exception e) { }
            // --- Основной текст ---
            curY -= 8;
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("Настоящая грамота подтверждает, что", fontTextSmall), textBlockX, curY, 0);
            curY -= textLineStep;
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(user.getFullName(), fontNameSmall), textBlockX, curY, 0);
            curY -= textLineStep;
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("занял(а) призовое место в олимпиаде:", fontTextSmall), textBlockX, curY, 0);
            curY -= textLineStep;
            // --- Центрированный блок: олимпиада, место, баллы ---
            curY -= 8;
            Font fontOlymp = new Font(bf, 24, Font.BOLD, new java.awt.Color(34,197,94)); // зелёный
            Font fontPlace = new Font(bf, 30, Font.BOLD, new java.awt.Color(239,68,68)); // красный
            Font fontScore = new Font(bf, 16, Font.NORMAL, new java.awt.Color(120,120,120));
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(olympiad.getName(), fontOlymp), centerX, curY, 0);
            curY -= textLineStep;
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Место: " + result.getPlace(), fontPlace), centerX, curY, 0);
            curY -= textLineStep-8;
            if (result.getTotalScore() != null && result.getMaxPossibleScore() != null) {
                String scoreStr = String.format("Набрано баллов: %.2f из %.2f", result.getTotalScore(), result.getMaxPossibleScore());
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(scoreStr, fontScore), centerX, curY, 0);
                curY -= textLineStep-14;
            }
            // --- Дата и номер слева рядом с QR-кодом ---
            String certNumberText = "Уникальный номер: " + certNumber;
            String dateText = "Дата выдачи: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            float infoLeftX = 170;
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(dateText, fontSmall), infoLeftX, 120, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(certNumberText, fontSmall), infoLeftX, 90, 0);
            // --- QR-код внизу слева ---
            String qrText = "https://olimpiada.ru/diploma/" + user.getId() + "/" + olympiad.getId();
            int qrSize = 90;
            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, qrSize, qrSize);
                BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
                ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
                ImageIO.write(qrImage, "PNG", qrBaos);
                Image qrPdfImg = Image.getInstance(qrBaos.toByteArray());
                qrPdfImg.scaleAbsolute(qrSize, qrSize);
                qrPdfImg.setAbsolutePosition(60, 60);
                document.add(qrPdfImg);
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("Проверить подлинность", new Font(bf, 12, Font.ITALIC, new java.awt.Color(37,99,235))), 60, 50, 0);
            } catch (Exception e) {}
            // --- Печать выше подписи и оргкомитета ---
            PdfContentByte stamp = writer.getDirectContent();
            float stampX = pageWidth-180;
            float stampY = 130f; // чуть выше
            stamp.setLineWidth(2.2f);
            stamp.setColorStroke(mainColor);
            stamp.circle(stampX, stampY, 52);
            stamp.stroke();
            stamp.beginText();
            stamp.setFontAndSize(bf, 16);
            stamp.setColorFill(new java.awt.Color(37,99,235));
            stamp.showTextAligned(Element.ALIGN_CENTER, "Олимпиада", stampX, stampY+14, 0);
            stamp.setFontAndSize(bf, 14);
            stamp.showTextAligned(Element.ALIGN_CENTER, "МИ ВлГУ", stampX, stampY-6, 0);
            stamp.endText();
            // --- Подпись и оргкомитет чуть выше ---
            String[] signatures = {"Иванов И.И.", "Петров П.П.", "Сидоров А.А.", "Кузнецов В.В.", "Смирнов Д.Д."};
            String randomSignature = signatures[(int)(Math.random()*signatures.length)];
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("_______________________", fontSmall), pageWidth-120, 110, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase(randomSignature, new Font(bf, 18, Font.ITALIC, new java.awt.Color(30,41,59))), pageWidth-120, 90, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Оргкомитет Олимпиады", fontSmall), pageWidth-120, 70, 0);
            document.close();
            response.getOutputStream().write(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            response.getOutputStream().write("Ошибка генерации грамоты".getBytes(StandardCharsets.UTF_8));
        }
    }
} 