package com.olimpiada.service;

import com.olimpiada.entity.Result;
import com.olimpiada.repository.ResultRepository;
import com.olimpiada.entity.Participation;
import com.olimpiada.entity.User;
import com.olimpiada.entity.Olympiad;
import com.olimpiada.entity.Answer;
import com.olimpiada.entity.ResultStatus;
import com.olimpiada.repository.AnswerRepository;
import com.olimpiada.repository.OlympiadRepository;
import com.olimpiada.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.olimpiada.repository.RatingRepository;
import com.olimpiada.entity.Task;
import com.olimpiada.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.olimpiada.service.UserAnswerService;
import com.olimpiada.entity.UserAnswer;

import java.util.List;

@Service
public class ResultService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResultService.class);
    
    @Autowired
    private ResultRepository resultRepository;
    
    @Autowired
    private AnswerRepository answerRepository;
    
    @Autowired
    private OlympiadRepository olympiadRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserAnswerService userAnswerService;
    
    @Autowired
    private com.olimpiada.service.CertificateService certificateService;
    
    public List<Result> findByUserIdAndOlympiadId(Long userId, Long olympiadId) {
        return resultRepository.findByUserIdAndOlympiadId(userId, olympiadId);
    }
    
    public List<Result> findByUserId(Long userId) {
        return resultRepository.findByUserId(userId);
    }
    
    public List<Result> findByOlympiadId(Long olympiadId) {
        return resultRepository.findByOlympiadId(olympiadId);
    }
    
    public Result save(Result result) {
        return resultRepository.save(result);
    }
    
    /**
     * Рассчитывает и сохраняет результат для participation (user + olympiad)
     */
    public Result calculateAndSaveResultForParticipation(Participation participation) {
        if (participation == null) {
            logger.warn("Attempt to calculate result for null participation");
            return null;
        }
        
        User user = participation.getUser();
        Olympiad olympiad = participation.getOlympiad();
        if (user == null || olympiad == null) {
            logger.warn("User or Olympiad is null for participation. User: {}, Olympiad: {}", 
                user == null ? "null" : user.getId(), 
                olympiad == null ? "null" : olympiad.getId());
            return null;
        }
        
        Long userId = user.getId();
        Long olympiadId = olympiad.getId();
        
        logger.info("Starting result calculation for user {} in olympiad {}", userId, olympiadId);
        
        // Получаем все задания олимпиады для расчета максимального балла
        List<Task> tasks = taskRepository.findByOlympiadId(olympiadId);
        logger.debug("Found {} tasks for olympiad {}", tasks.size(), olympiadId);
        
        float maxPossibleScore = (float) tasks.stream()
            .mapToDouble(task -> {
                float taskScore = task.getMaxScore();
                logger.debug("Task {}: maxScore={}", 
                    task.getId(), task.getMaxScore());
                return taskScore;
            })
            .sum();
            
        logger.info("Max possible score for olympiad {}: {}", olympiadId, maxPossibleScore);
            
        // Получаем все ответы пользователя по олимпиаде через UserAnswerService
        List<UserAnswer> userAnswers = userAnswerService.findByUserAndOlympiad(userId, olympiadId);
        logger.debug("Found {} userAnswers for user {} in olympiad {}", userAnswers.size(), userId, olympiadId);

        // Мапа: taskId -> балл пользователя (или null)
        java.util.Map<Long, Float> userScores = new java.util.HashMap<>();
        for (UserAnswer ua : userAnswers) {
            if (ua.getTask() != null) {
                userScores.put(ua.getTask().getId(), ua.getScore() != null ? ua.getScore().floatValue() : 0f);
            }
        }
        // Суммируем баллы за все задания: если нет ответа — 0
        float totalScore = 0f;
        for (Task task : tasks) {
            float score = userScores.getOrDefault(task.getId(), 0f);
            logger.debug("Task {}: user score={}", task.getId(), score);
            totalScore += score;
        }
        logger.info("Total score for user {} in olympiad {}: {}", userId, olympiadId, totalScore);
            
        // Вычисляем процент выполнения
        float completionPercentage = maxPossibleScore > 0 ? (totalScore / maxPossibleScore) * 100 : 0;
        logger.info("Completion percentage for user {} in olympiad {}: {}%", 
            userId, olympiadId, String.format("%.2f", completionPercentage));
        
        // Создаем или обновляем результат
        Result result = resultRepository.findByUserIdAndOlympiadId(userId, olympiadId)
            .stream()
            .findFirst()
            .orElse(new Result());
            
        result.setUser(user);
        result.setOlympiad(olympiad);
        result.setTotalScore(totalScore);
        result.setMaxPossibleScore(maxPossibleScore);
        result.setCompletionPercentage(completionPercentage);
        result.setSubmissionDate(LocalDateTime.now());
        result.setStatus(ResultStatus.COMPLETED);
        
        Result savedResult = resultRepository.save(result);
        logger.info("Saved result for user {} in olympiad {} with id {}", 
            userId, olympiadId, savedResult.getId());
        
        // Автоматическая отправка грамоты призёрам
        if (savedResult.getStatus() == ResultStatus.COMPLETED && savedResult.getPlace() != null && savedResult.getPlace() >= 1 && savedResult.getPlace() <= 3) {
            try {
                certificateService.sendDiplomaEmail(user, olympiad);
            } catch (Exception e) {
                logger.error("Ошибка отправки грамоты призёру на email", e);
            }
        }
        
        // После сохранения результата:
        if (savedResult.getStatus() == ResultStatus.COMPLETED) {
            certificateService.sendCertificateEmail(user, olympiad);
        }
        
        return savedResult;
    }
    
    /**
     * Пересчитывает рейтинг и места для олимпиады
     */
    public void updateRatingByOlympiadId(Long olympiadId) {
        logger.info("Starting rating update for olympiad {}", olympiadId);
        
        com.olimpiada.entity.Olympiad olympiad = olympiadRepository.findById(olympiadId)
            .orElseThrow(() -> {
                logger.error("Olympiad not found with id {}", olympiadId);
                return new RuntimeException("Олимпиада не найдена");
            });
            
        java.util.List<Result> results = resultRepository.findByOlympiadId(olympiadId).stream()
            .sorted((r1, r2) -> Float.compare(r2.getTotalScore(), r1.getTotalScore()))
            .toList();
            
        logger.debug("Found {} results for olympiad {}", results.size(), olympiadId);
        
        com.olimpiada.entity.Rating rating = ratingRepository.findByOlympiadId(olympiadId)
            .stream()
            .findFirst()
            .orElse(new com.olimpiada.entity.Rating());
            
        rating.setOlympiad(olympiad);
        float totalRatingScore = results.stream()
            .map(Result::getTotalScore)
            .reduce(0f, Float::sum);
        rating.setTotalScore(totalRatingScore);
        
        ratingRepository.save(rating);
        logger.info("Updated rating for olympiad {} with total score {}", olympiadId, totalRatingScore);
        
        for (int i = 0; i < results.size(); i++) {
            Result result = results.get(i);
            result.setPlace(i + 1);
            result.setRating(rating);
            resultRepository.save(result);
            logger.debug("Set place {} for user {} in olympiad {}", 
                i + 1, result.getUser().getId(), olympiadId);
        }
        
        logger.info("Completed rating update for olympiad {}", olympiadId);
    }
} 