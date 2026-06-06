package com.eduquest.service;

import com.eduquest.dto.*;
import com.eduquest.repository.QuizAttemptRepository;
import com.eduquest.repository.UserBadgeRepository;
import com.eduquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserRepository userRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserBadgeRepository badgeRepository;

    public ProgressSummaryDto getSummary(String userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalQuizzes  = attemptRepository.countByUserId(userId);
        Double avgAccuracy = attemptRepository.findAvgAccuracyByUserId(userId);
        int weeklyXp       = attemptRepository.sumXpEarnedSince(userId,
                LocalDateTime.now().minusDays(7));
        long badgeCount    = badgeRepository.findByUserIdOrderByEarnedAtDesc(userId).size();

        return ProgressSummaryDto.builder()
                .totalXp(user.getTotalXp()).level(user.getLevel())
                .streakCount(user.getStreakCount()).totalQuizzes((int) totalQuizzes)
                .avgAccuracy(avgAccuracy != null ? Math.round(avgAccuracy * 10.0) / 10.0 : 0.0)
                .weeklyXp(weeklyXp).badgeCount((int) badgeCount)
                .build();
    }

    public List<SubjectProgressDto> getSubjectProgress(String userId) {
        List<Object[]> raw = attemptRepository.findSubjectStatsForUser(userId);
        return raw.stream().map(row -> SubjectProgressDto.builder()
                .subject((String) row[0])
                .totalAttempts(((Long) row[1]).intValue())
                .avgAccuracy(row[2] != null ? Math.round((Double) row[2] * 10.0) / 10.0 : 0.0)
                .build()
        ).collect(Collectors.toList());
    }

    public List<DailyXpDto> getWeeklyXp(String userId) {
        List<DailyXpDto> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime start = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end   = start.plusDays(1);
            int xp = attemptRepository.sumXpEarnedSince(userId, start);
            result.add(new DailyXpDto(start.toLocalDate().toString(), xp));
        }
        return result;
    }

    public List<BadgeDto> getBadges(String userId) {
        return badgeRepository.findByUserIdOrderByEarnedAtDesc(userId)
                .stream().map(b -> new BadgeDto(b.getBadgeName(), b.getBadgeEmoji(),
                        b.getBadgeDescription(), b.getEarnedAt().toString()))
                .collect(Collectors.toList());
    }

    public List<HeatmapEntryDto> getHeatmap(String userId) {
        // Returns last 90 days of activity counts
        List<HeatmapEntryDto> result = new ArrayList<>();
        for (int i = 89; i >= 0; i--) {
            LocalDateTime start = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0);
            LocalDateTime end   = start.plusDays(1);
            int xp = attemptRepository.sumXpEarnedSince(userId, start);
            int level = xp == 0 ? 0 : xp < 20 ? 1 : xp < 50 ? 2 : xp < 100 ? 3 : 4;
            result.add(new HeatmapEntryDto(start.toLocalDate().toString(), xp, level));
        }
        return result;
    }
}
