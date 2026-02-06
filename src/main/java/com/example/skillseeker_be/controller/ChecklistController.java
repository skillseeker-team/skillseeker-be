package com.example.skillseeker_be.controller;

import com.example.skillseeker_be.entity.FeedbackChecklistItem;
import com.example.skillseeker_be.exception.BadRequestException;
import com.example.skillseeker_be.exception.NotFoundException;
import com.example.skillseeker_be.repository.FeedbackChecklistItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklist", description = "체크리스트 항목 관리 API")
public class ChecklistController {

    private final FeedbackChecklistItemRepository checklistItemRepository;

    @PatchMapping("/{itemId}")
    @Operation(summary = "체크리스트 완료 처리", description = "체크리스트 항목을 TODO에서 DONE으로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "이미 완료된 항목"),
            @ApiResponse(responseCode = "404", description = "항목을 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable Long itemId) {
        FeedbackChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Checklist item not found: " + itemId));

        if ("DONE".equals(item.getStatus())) {
            throw new BadRequestException("Checklist item is already DONE");
        }

        item.setStatus("DONE");
        checklistItemRepository.save(item);

        return ResponseEntity.ok(Map.of(
                "id", item.getId(),
                "status", item.getStatus()
        ));
    }
}
