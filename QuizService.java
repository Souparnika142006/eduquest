package com.eduquest.service;

import com.eduquest.dto.*;
import com.eduquest.model.*;
import com.eduquest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository attemptRepository;
    private final BadgeService badgeService;
    private final LeaderboardService leaderboardService;

    private static final int BASE_XP_EASY   = 10;
    private static final int BASE_XP_MEDIUM = 25;
    private static final int BASE_XP_HARD   = 50;
    private static final double STREAK_BONUS_MULTIPLIER = 0.1; // 10% bonus per streak day (capped at 50%)

    public Page<QuizSummaryDto> getQuizzes(String subject, Quiz.Difficulty difficulty, Pageable pageable) {
        if (subject != null && difficulty != null)
            return quizRepository.findBySubjectAndDifficulty(subject, difficulty, pageable).map(this::toSummaryDto);
        if (subject != null)
            return quizRepository.findBySubjectContainingIgnoreCase(subject, pageable).map(this::toSummaryDto);
        if (difficulty != null)
            return quizRepository.findByDifficulty(difficulty, pageable).map(this::toSummaryDto);
        return quizRepository.findByIsActiveTrue(pageable).map(this::toSummaryDto);
    }

    public QuizDetailDto getQuizById(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found: " + quizId));
        return toDetailDto(quiz);
    }

    @Transactional
    public AttemptResultDto submitAttempt(String quizId, String userId, SubmitAttemptRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Calculate score
        var questions = quiz.getQuestions();
        int correct = 0;
        for (int i = 0; i < Math.min(questions.size(), request.getAnswers().size()); i++) {
            if (questions.get(i).getCorrectOptionIndex().equals(request.getAnswers().get(i))) correct++;
        }

        // Calculate XP with streak bonus
        int baseXp = getBaseXp(quiz.getDifficulty());
        double accuracyRatio = (double) correct / questions.size();
        double streakMultiplier = 1 + Math.min(user.getStreakCount() * STREAK_BONUS_MULTIPLIER, 0.5);
        int xpEarned = (int) Math.round(baseXp * accuracyRatio * streakMultiplier);

        // Update user XP, level, streak
        user.setTotalXp(user.getTotalXp() + xpEarned);
        user.updateLevel();
        updateStreak(user);

        // Save attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user).quiz(quiz).score(correct)
                .totalQuestions(questions.size()).xpEarned(xpEarned)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .answers(request.getAnswers()).build();
        attemptRepository.save(attempt);
        userRepository.save(user);

        // Check and award badges asynchronously
        badgeService.checkAndAwardBadges(user, attempt);

        // Update leaderboard cache
        leaderboardService.updateUserScore(userId, user.getTotalXp());

        log.info("User {} completed quiz {} — score {}/{}, XP +{}", userId, quizId, correct, questions.size(), xpEarned);

        return AttemptResultDto.builder()
                .attemptId(attempt.getId()).score(correct).totalQuestions(questions.size())
                .xpEarned(xpEarned).newTotalXp(user.getTotalXp()).newLevel(user.getLevel())
                .streakCount(user.getStreakCount()).accuracyPercent(attempt.getAccuracyPercent())
                .streakBonus(streakMultiplier > 1)
                .build();
    }

    private void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActive = user.getLastActiveDate() != null ? user.getLastActiveDate().toLocalDate() : null;
        if (lastActive == null || lastActive.isBefore(today.minusDays(1))) {
            user.setStreakCount(1);
        } else if (lastActive.equals(today.minusDays(1))) {
            user.setStreakCount(user.getStreakCount() + 1);
        }
        user.setLastActiveDate(LocalDateTime.now());
    }

    private int getBaseXp(Quiz.Difficulty d) {
        return switch (d) {
            case EASY   -> BASE_XP_EASY;
            case MEDIUM -> BASE_XP_MEDIUM;
            case HARD   -> BASE_XP_HARD;
        };
    }

    public List<String> getAllSubjects() {
        return quizRepository.findDistinctSubjects();
    }

    public QuizDetailDto getDailyChallenge() {
        // Rotates daily based on hash of current date
        int dayOfYear = LocalDate.now().getDayOfYear();
        var quizzes = quizRepository.findByIsActiveTrue(Pageable.unpaged()).getContent();
        return toDetailDto(quizzes.get(dayOfYear % quizzes.size()));
    }

    public List<AttemptSummaryDto> getUserAttempts(String userId, Pageable pageable) {
        return attemptRepository.findByUserIdOrderByAttemptedAtDesc(userId, pageable)
                .stream().map(this::toAttemptSummaryDto).collect(Collectors.toList());
    }

    @Transactional
    public QuizDetailDto createQuiz(CreateQuizRequest request) {
        // Map request → Quiz entity and save
        Quiz quiz = Quiz.builder()
                .title(request.getTitle()).subject(request.getSubject())
                .description(request.getDescription()).difficulty(request.getDifficulty())
                .xpReward(getBaseXp(request.getDifficulty())).timeLimitSeconds(request.getTimeLimitSeconds())
                .build();
        return toDetailDto(quizRepository.save(quiz));
    }

    // ── DTO Mappers ──
    private QuizSummaryDto toSummaryDto(Quiz q) {
        return new QuizSummaryDto(q.getId(), q.getTitle(), q.getSubject(),
                q.getDifficulty().name(), q.getQuestions().size(), q.getXpReward());
    }
    private QuizDetailDto toDetailDto(Quiz q) { return new QuizDetailDto(q); }
    private AttemptSummaryDto toAttemptSummaryDto(QuizAttempt a) { return new AttemptSummaryDto(a); }
}
