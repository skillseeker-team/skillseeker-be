package com.example.skillseeker_be.dto;

import com.example.skillseeker_be.entity.Interview;
import com.example.skillseeker_be.entity.InterviewQuestion;
import lombok.Builder;
import lombok.Getter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InterviewResponse {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Long id;
    private String company;
    private String role;
    private LocalDate interviewDate;
    private String tension;
    private List<String> conditionMethods;
    private Integer satisfactionScore;
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
        List<String> methods = null;
        if (interview.getConditionMethods() != null) {
            try {
                methods = MAPPER.readValue(interview.getConditionMethods(), new TypeReference<>() {});
            } catch (Exception e) {
                methods = List.of();
            }
        }
        return InterviewResponse.builder()
                .id(interview.getId())
                .company(interview.getCompanyName())
                .role(interview.getPosition())
                .interviewDate(interview.getInterviewDate())
                .tension(interview.getTension())
                .conditionMethods(methods)
                .satisfactionScore(interview.getSatisfactionScore())
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
