package com.eduquest.dto;

import lombok.*;

// ── Progress DTOs ─────────────────────────────────────────
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProgressSummaryDto {
    private int totalXp, level, streakCount, totalQuizzes, weeklyXp, badgeCount;
    private double avgAccuracy;
}

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
class SubjectProgressDto {
    private String subject;
    private int totalAttempts;
    private double avgAccuracy;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class DailyXpDto {
    private String date;
    private int xp;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class HeatmapEntryDto {
    private String date;
    private int xp;
    private int level; // 0-4 intensity
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class BadgeDto {
    private String name, emoji, description, earnedAt;
}
