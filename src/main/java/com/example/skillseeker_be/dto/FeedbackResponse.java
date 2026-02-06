package com.example.skillseeker_be.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class FeedbackResponse {

    private Long id;
    private Long interviewId;
    private String status;
    private List<String> overallSummary;
    private String improvementOne;
    private List<WeaknessTagResponse> weaknessTags;
    private List<ChecklistItemResponse> checklistItems;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class WeaknessTagResponse {
        private String tag;
        private String label;
        private String description;
        private String reason;
    }

    @Getter
    @Builder
    public static class ChecklistItemResponse {
        private Long id;
        private String templateId;
        private String renderedText;
        private String status;
    }
}
