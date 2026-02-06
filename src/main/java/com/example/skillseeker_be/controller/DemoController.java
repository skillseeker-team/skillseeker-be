package com.example.skillseeker_be.controller;

import com.example.skillseeker_be.service.DemoSeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Tag(name = "Demo", description = "데모 데이터 관리 API")
public class DemoController {

    private final DemoSeedService demoSeedService;

    @PostMapping("/seed")
    @Operation(summary = "데모 데이터 생성", description = "데모 유저의 기존 데이터를 초기화하고 6개 면접, 20+ 질문, 3+ 피드백 리포트를 생성합니다.")
    public ResponseEntity<Map<String, Object>> seed() {
        return ResponseEntity.ok(demoSeedService.seed());
    }
}
