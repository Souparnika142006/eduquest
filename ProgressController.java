package com.eduquest.controller;

import com.eduquest.dto.*;
import com.eduquest.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Student progress analytics and reports")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/summary")
    @Operation(summary = "Get overall progress summary for the logged-in user")
    public ResponseEntity<ApiResponse<ProgressSummaryDto>> getSummary(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success("Progress summary", progressService.getSummary(userId)));
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get subject-wise breakdown of performance")
    public ResponseEntity<ApiResponse<java.util.List<SubjectProgressDto>>> getSubjectProgress(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success("Subject progress", progressService.getSubjectProgress(userId)));
    }

    @GetMapping("/weekly-xp")
    @Operation(summary = "Get XP earned per day for the last 7 days")
    public ResponseEntity<ApiResponse<java.util.List<DailyXpDto>>> getWeeklyXp(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success("Weekly XP", progressService.getWeeklyXp(userId)));
    }

    @GetMapping("/badges")
    @Operation(summary = "Get all badges earned by the user")
    public ResponseEntity<ApiResponse<java.util.List<BadgeDto>>> getBadges(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success("Badges", progressService.getBadges(userId)));
    }

    @GetMapping("/heatmap")
    @Operation(summary = "Get activity heatmap data for the past 90 days")
    public ResponseEntity<ApiResponse<java.util.List<HeatmapEntryDto>>> getHeatmap(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.success("Heatmap data", progressService.getHeatmap(userId)));
    }
}
