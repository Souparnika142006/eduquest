package com.eduquest.controller;

import com.eduquest.dto.*;
import com.eduquest.model.Quiz;
import com.eduquest.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz management and attempt endpoints")
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    @Operation(summary = "Get all available quizzes (paginated, filterable)")
    public ResponseEntity<ApiResponse<Page<QuizSummaryDto>>> getQuizzes(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Quiz.Difficulty difficulty,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Quizzes fetched",
                quizService.getQuizzes(subject, difficulty, pageable)));
    }

    @GetMapping("/{quizId}")
    @Operation(summary = "Get quiz details with questions")
    public ResponseEntity<ApiResponse<QuizDetailDto>> getQuizById(@PathVariable String quizId) {
        return ResponseEntity.ok(ApiResponse.success("Quiz fetched", quizService.getQuizById(quizId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Create a new quiz (Instructor/Admin only)")
    public ResponseEntity<ApiResponse<QuizDetailDto>> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Quiz created", quizService.createQuiz(request)));
    }

    @PostMapping("/{quizId}/attempt")
    @Operation(summary = "Submit quiz answers and get results with XP")
    public ResponseEntity<ApiResponse<AttemptResultDto>> submitAttempt(
            @PathVariable String quizId,
            @Valid @RequestBody SubmitAttemptRequest request,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success("Attempt submitted",
                quizService.submitAttempt(quizId, userId, request)));
    }

    @GetMapping("/my-attempts")
    @Operation(summary = "Get current user's quiz attempt history")
    public ResponseEntity<ApiResponse<List<AttemptSummaryDto>>> getMyAttempts(
            @AuthenticationPrincipal String userId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Attempts fetched",
                quizService.getUserAttempts(userId, pageable)));
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get list of all available subjects")
    public ResponseEntity<ApiResponse<List<String>>> getSubjects() {
        return ResponseEntity.ok(ApiResponse.success("Subjects fetched", quizService.getAllSubjects()));
    }

    @GetMapping("/daily-challenge")
    @Operation(summary = "Get today's daily challenge quiz")
    public ResponseEntity<ApiResponse<QuizDetailDto>> getDailyChallenge() {
        return ResponseEntity.ok(ApiResponse.success("Daily challenge fetched", quizService.getDailyChallenge()));
    }
}
