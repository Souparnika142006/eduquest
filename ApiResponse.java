package com.eduquest.dto;

import lombok.*;
import java.util.List;

// ── Generic API Response Wrapper ──────────────────────────
@Getter @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}

// ── Auth DTOs ─────────────────────────────────────────────
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class RegisterRequest {
    @jakarta.validation.constraints.NotBlank private String name;
    @jakarta.validation.constraints.Email     private String email;
    @jakarta.validation.constraints.Size(min = 8) private String password;
    private String collegeName;
    private String branch;
    private Integer yearOfStudy;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class LoginRequest {
    @jakarta.validation.constraints.Email  private String email;
    @jakarta.validation.constraints.NotBlank private String password;
}

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String name;
    private String email;
    private int level;
    private int totalXp;
    private String role;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class RefreshRequest { private String refreshToken; }

// ── Quiz DTOs ─────────────────────────────────────────────
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
class QuizSummaryDto {
    private String id, title, subject, difficulty;
    private int questionCount, xpReward;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class QuizDetailDto {
    private String id, title, subject, difficulty, description;
    private int xpReward, timeLimitSeconds;
    private List<QuestionDto> questions;

    public QuizDetailDto(com.eduquest.model.Quiz q) {
        this.id = q.getId(); this.title = q.getTitle();
        this.subject = q.getSubject(); this.difficulty = q.getDifficulty().name();
        this.description = q.getDescription(); this.xpReward = q.getXpReward();
        this.timeLimitSeconds = q.getTimeLimitSeconds() != null ? q.getTimeLimitSeconds() : 300;
    }
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class QuestionDto {
    private String id, questionText, topicTag;
    private List<String> options;
    // Correct answer NOT sent to client; validated server-side
}

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
class SubmitAttemptRequest {
    private List<Integer> answers;
    private Integer timeTakenSeconds;
}

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
class AttemptResultDto {
    private String attemptId;
    private int score, totalQuestions, xpEarned, newTotalXp, newLevel, streakCount;
    private double accuracyPercent;
    private boolean streakBonus;
    private List<String> newBadges;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class AttemptSummaryDto {
    private String attemptId, quizTitle, subject;
    private int score, totalQuestions, xpEarned;
    private double accuracyPercent;
    private String attemptedAt;

    public AttemptSummaryDto(com.eduquest.model.QuizAttempt a) {
        this.attemptId = a.getId();
        this.score = a.getScore(); this.totalQuestions = a.getTotalQuestions();
        this.xpEarned = a.getXpEarned(); this.accuracyPercent = a.getAccuracyPercent();
        this.attemptedAt = a.getAttemptedAt().toString();
    }
}

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
class CreateQuizRequest {
    private String title, subject, description;
    private com.eduquest.model.Quiz.Difficulty difficulty;
    private Integer timeLimitSeconds;
    private List<QuestionDto> questions;
}

// ── Leaderboard DTOs ──────────────────────────────────────
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
class LeaderboardEntryDto {
    private long rank;
    private String userId, name, collegeName, branch, avatarEmoji;
    private int totalXp, level, streakCount;
}

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
class UserRankDto {
    private String userId;
    private long rank, totalParticipants;
    private int totalXp, percentile;
}

// ── AI Tutor DTOs ─────────────────────────────────────────
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class AiTutorRequest {
    private String message;
    private String topic;
    private List<ConversationTurn> history;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class AiTutorResponse {
    private String reply;
    private int tokensUsed;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class ConversationTurn {
    private String role;  // "user" | "assistant"
    private String content;
}
