package com.eduquest.controller;

import com.eduquest.dto.*;
import com.eduquest.service.LeaderboardService;
import com.eduquest.service.AiTutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

// ─── Leaderboard Controller ───────────────────────────────
@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Global, college, and subject leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/global")
    @Operation(summary = "Get global top 100 leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Global leaderboard",
                leaderboardService.getGlobalLeaderboard(limit)));
    }

    @GetMapping("/college")
    @Operation(summary = "Get leaderboard filtered by college")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getCollegeLeaderboard(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success("College leaderboard",
                leaderboardService.getCollegeLeaderboard(userId, limit)));
    }

    @GetMapping("/subject/{subject}")
    @Operation(summary = "Get subject-specific leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getSubjectLeaderboard(
            @PathVariable String subject,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Subject leaderboard",
                leaderboardService.getSubjectLeaderboard(subject, limit)));
    }

    @GetMapping("/my-rank")
    @Operation(summary = "Get current user's rank and nearby competitors")
    public ResponseEntity<ApiResponse<UserRankDto>> getMyRank(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success("Your rank", leaderboardService.getUserRank(userId)));
    }
}
