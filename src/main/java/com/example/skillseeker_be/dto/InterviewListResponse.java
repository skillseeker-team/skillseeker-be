package com.example.skillseeker_be.dto;

import com.example.skillseeker_be.entity.Interview;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewListResponse {

    private Long id;
    private String company;
    private String role;
    private LocalDate interviewDate;
    private int questionCount;
    private boolean hasFeedback;
    private LocalDateTime createdAt;

    public static InterviewListResponse from(Interview interview) {
        return InterviewListResponse.builder()
                .id(interview.getId())
                .company(interview.getCompanyName())
                .role(interview.getPosition())
                .interviewDate(interview.getInterviewDate())
                .questionCount(interview.getQuestions().size())
                .hasFeedback(interview.getFeedbackReport() != null)
                .createdAt(interview.getCreatedAt())
                .build();
    }
}
