package com.example.skillseeker_be.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class InterviewCreateRequest {

    @NotBlank(message = "company is required")
    private String company;

    @NotBlank(message = "role is required")
    private String role;

    @NotNull(message = "interviewDate is required")
    private LocalDate interviewDate;

    private Integer atmosphereScore;
    private Integer tensionChangeScore;
    private String memo;

    @NotEmpty(message = "at least one question is required")
    @Valid
    private List<QuestionRequest> questions;

    @Getter
    @Setter
    public static class QuestionRequest {
        @NotBlank(message = "questionText is required")
        private String questionText;
        private String answerText;
        private Boolean isHardest;
        private Boolean isBest;
    }
}
