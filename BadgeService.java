package com.eduquest.service;

import com.eduquest.model.QuizAttempt;
import com.eduquest.model.User;
import com.eduquest.model.UserBadge;
import com.eduquest.repository.UserBadgeRepository;
import com.eduquest.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final UserBadgeRepository badgeRepository;
    private final QuizAttemptRepository attemptRepository;

    // Badge definitions — (name, emoji, description, check logic)
    private record BadgeDef(String name, String emoji, String description) {}

    private static final List<BadgeDef> BADGE_DEFS = List.of(
        new BadgeDef("First Quiz",    "🎉", "Completed your very first quiz"),
        new BadgeDef("Speed Demon",   "⚡", "Finished a quiz in under 60 seconds"),
        new BadgeDef("Perfect 10",    "💯", "Scored 100% on a quiz"),
        new BadgeDef("7-Day Streak",  "🔥", "Studied 7 days in a row"),
        new BadgeDef("30-Day Streak", "🌟", "Studied 30 days in a row"),
        new BadgeDef("Quiz Master",   "🏅", "Completed 50 quizzes"),
        new BadgeDef("Century",       "💎", "Completed 100 quizzes"),
        new BadgeDef("Hard Mode",     "🔴", "Scored 80%+ on a Hard quiz"),
        new BadgeDef("Consistency",   "📅", "Completed at least 1 quiz per day for 14 days"),
        new BadgeDef("Scholar",       "📚", "Reached Level 10"),
        new BadgeDef("Grandmaster",   "👑", "Reached Level 25"),
        new BadgeDef("Top 10",        "🏆", "Entered the global top 10 leaderboard")
    );

    @Async
    @Transactional
    public void checkAndAwardBadges(User user, QuizAttempt latestAttempt) {
        List<String> existingBadges = badgeRepository.findBadgeNamesByUserId(user.getId());
        List<UserBadge> newBadges = new ArrayList<>();
        long totalAttempts = attemptRepository.countByUserId(user.getId());

        // First Quiz
        if (!existingBadges.contains("First Quiz") && totalAttempts == 1) {
            newBadges.add(buildBadge(user, "First Quiz", "🎉", "Completed your very first quiz"));
        }

        // Speed Demon
        if (!existingBadges.contains("Speed Demon") &&
                latestAttempt.getTimeTakenSeconds() != null &&
                latestAttempt.getTimeTakenSeconds() <= 60) {
            newBadges.add(buildBadge(user, "Speed Demon", "⚡", "Finished a quiz in under 60 seconds"));
        }

        // Perfect 10
        if (!existingBadges.contains("Perfect 10") &&
                latestAttempt.getScore().equals(latestAttempt.getTotalQuestions())) {
            newBadges.add(buildBadge(user, "Perfect 10", "💯", "Scored 100% on a quiz"));
        }

        // Streak badges
        if (!existingBadges.contains("7-Day Streak") && user.getStreakCount() >= 7) {
            newBadges.add(buildBadge(user, "7-Day Streak", "🔥", "Studied 7 days in a row"));
        }
        if (!existingBadges.contains("30-Day Streak") && user.getStreakCount() >= 30) {
            newBadges.add(buildBadge(user, "30-Day Streak", "🌟", "Studied 30 days in a row"));
        }

        // Quiz count milestones
        if (!existingBadges.contains("Quiz Master") && totalAttempts >= 50) {
            newBadges.add(buildBadge(user, "Quiz Master", "🏅", "Completed 50 quizzes"));
        }
        if (!existingBadges.contains("Century") && totalAttempts >= 100) {
            newBadges.add(buildBadge(user, "Century", "💎", "Completed 100 quizzes"));
        }

        // Hard mode expert
        if (!existingBadges.contains("Hard Mode") &&
                "HARD".equals(latestAttempt.getQuiz().getDifficulty().name()) &&
                latestAttempt.getAccuracyPercent() >= 80) {
            newBadges.add(buildBadge(user, "Hard Mode", "🔴", "Scored 80%+ on a Hard quiz"));
        }

        // Level badges
        if (!existingBadges.contains("Scholar") && user.getLevel() >= 10) {
            newBadges.add(buildBadge(user, "Scholar", "📚", "Reached Level 10"));
        }
        if (!existingBadges.contains("Grandmaster") && user.getLevel() >= 25) {
            newBadges.add(buildBadge(user, "Grandmaster", "👑", "Reached Level 25"));
        }

        if (!newBadges.isEmpty()) {
            badgeRepository.saveAll(newBadges);
            log.info("Awarded {} new badge(s) to user {}: {}", newBadges.size(), user.getId(),
                    newBadges.stream().map(UserBadge::getBadgeName).toList());
        }
    }

    private UserBadge buildBadge(User user, String name, String emoji, String description) {
        return UserBadge.builder()
                .user(user).badgeName(name)
                .badgeEmoji(emoji).badgeDescription(description)
                .build();
    }
}
