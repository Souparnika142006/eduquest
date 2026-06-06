package com.eduquest.controller;

import com.eduquest.dto.*;
import com.eduquest.service.AiTutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/ai-tutor")
@RequiredArgsConstructor
@Tag(name = "AI Tutor", description = "Claude-powered AI tutor endpoints")
public class AiTutorController {

    private final AiTutorService aiTutorService;

    @PostMapping("/ask")
    @Operation(summary = "Send a message to the AI tutor and get a response")
    public Mono<ResponseEntity<ApiResponse<AiTutorResponse>>> ask(
            @Valid @RequestBody AiTutorRequest request,
            @AuthenticationPrincipal String userId) {
        return aiTutorService.askTutor(userId, request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("AI response", response)));
    }

    @GetMapping("/topics")
    @Operation(summary = "Get list of tutoring topics available")
    public ResponseEntity<ApiResponse<java.util.List<String>>> getTopics() {
        var topics = java.util.List.of(
            "Data Structures", "Algorithms", "Database Management",
            "Operating Systems", "Computer Networks", "AI & Machine Learning",
            "Mathematics", "Discrete Mathematics", "Software Engineering"
        );
        return ResponseEntity.ok(ApiResponse.success("Topics fetched", topics));
    }
}
