package com.example.skillseeker_be.dto;

import com.example.skillseeker_be.entity.Interview;
import com.example.skillseeker_be.entity.InterviewQuestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InterviewResponse {

    private Long id;
    private String company;
    private String role;
    private LocalDate interviewDate;
    private String tension;
    private String memo;
    private LocalDateTime createdAt;
    private List<QuestionResponse> questions;

    @Getter
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String answerText;
        private Boolean isHardest;
        private Boolean isBest;
        private String category;
        private String questionKey;
    }

    public static InterviewResponse from(Interview interview) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .company(interview.getCompanyName())
                .role(interview.getPosition())
                .interviewDate(interview.getInterviewDate())
                .tension(interview.getTension())
                .memo(interview.getMemo())
                .createdAt(interview.getCreatedAt())
                .questions(interview.getQuestions().stream()
                        .map(InterviewResponse::toQuestionResponse)
                        .toList())
                .build();
    }

    private static QuestionResponse toQuestionResponse(InterviewQuestion q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .questionText(q.getQuestionTitle())
                .answerText(q.getAnswer())
                .isHardest(q.getIsHardest())
                .isBest(q.getIsBest())
                .category(q.getCategory())
                .questionKey(q.getQuestionKey())
                .build();
    }
}
