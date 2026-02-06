package com.example.skillseeker_be.controller;

import com.example.skillseeker_be.dto.FeedbackResponse;
import com.example.skillseeker_be.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "AI 피드백 생성 및 조회 API")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/api/interviews/{id}/feedback:ai")
    @Operation(summary = "AI 피드백 생성", description = "Gemini를 사용하여 면접 피드백을 생성합니다. 동일 payload면 캐시를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "피드백 생성/캐시 반환 성공"),
            @ApiResponse(responseCode = "400", description = "AI 생성 실패 (재시도 가능)"),
            @ApiResponse(responseCode = "404", description = "면접을 찾을 수 없음")
    })
    public ResponseEntity<FeedbackResponse> generateAiFeedback(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.generateAiReport(id));
    }

    @GetMapping("/api/interviews/{id}/feedback")
    @Operation(summary = "피드백 조회", description = "면접의 피드백 리포트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "피드백을 찾을 수 없음")
    })
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getFeedback(id));
    }
}
