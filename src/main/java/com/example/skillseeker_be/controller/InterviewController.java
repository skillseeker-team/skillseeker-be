package com.example.skillseeker_be.controller;

import com.example.skillseeker_be.dto.InterviewCreateRequest;
import com.example.skillseeker_be.dto.InterviewListResponse;
import com.example.skillseeker_be.dto.InterviewResponse;
import com.example.skillseeker_be.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Tag(name = "Interview", description = "면접 CRUD API")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @Operation(summary = "면접 생성", description = "새로운 면접 기록을 생성합니다. 질문 중 정확히 1개가 hardest여야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "면접 생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패")
    })
    public ResponseEntity<InterviewResponse> create(@Valid @RequestBody InterviewCreateRequest request) {
        InterviewResponse response = interviewService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "면접 목록 조회", description = "데모 유저의 전체 면접 목록을 조회합니다.")
    public ResponseEntity<List<InterviewListResponse>> list() {
        return ResponseEntity.ok(interviewService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "면접 상세 조회", description = "면접 ID로 상세 정보와 질문 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "면접을 찾을 수 없음")
    })
    public ResponseEntity<InterviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "면접 삭제", description = "면접을 삭제합니다. 관련 질문, 피드백, 체크리스트가 함께 삭제됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "면접을 찾을 수 없음")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
