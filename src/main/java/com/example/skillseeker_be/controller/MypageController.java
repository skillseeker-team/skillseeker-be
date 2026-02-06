package com.example.skillseeker_be.controller;

import com.example.skillseeker_be.dto.MypageInsightsResponse;
import com.example.skillseeker_be.dto.MypageNarrativeResponse;
import com.example.skillseeker_be.dto.MypageSummaryResponse;
import com.example.skillseeker_be.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 통계 및 분석 API")
public class MypageController {

    private final MypageService mypageService;

    @GetMapping("/summary")
    @Operation(summary = "통계 요약 조회", description = "SQL 집계 기반 면접 통계를 조회합니다 (LLM 미사용).")
    public ResponseEntity<MypageSummaryResponse> summary() {
        return ResponseEntity.ok(mypageService.getSummary());
    }

    @GetMapping("/narrative")
    @Operation(summary = "내러티브 조회", description = "통계 기반 템플릿 문장을 반환합니다 (LLM 미사용).")
    public ResponseEntity<MypageNarrativeResponse> narrative() {
        return ResponseEntity.ok(mypageService.getNarrative());
    }

    @PostMapping("/insights:ai")
    @Operation(summary = "AI 인사이트 생성", description = "통계 요약 JSON만 사용하여 AI 인사이트를 생성합니다. summary_hash로 캐싱됩니다.")
    public ResponseEntity<MypageInsightsResponse> insights() {
        return ResponseEntity.ok(mypageService.getInsights());
    }
}
