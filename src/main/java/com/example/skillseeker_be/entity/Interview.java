package com.example.skillseeker_be.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
@Getter
@NoArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(nullable = false, length = 200)
    private String position;

    @Column(name = "interview_date", nullable = false)
    private LocalDate interviewDate;

    @Column(length = 50)
    private String tension;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewQuestion> questions = new ArrayList<>();

    @OneToOne(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private FeedbackReport feedbackReport;

    @Builder
    public Interview(Long userId, String companyName, String position,
                     LocalDate interviewDate, String tension, String memo) {
        this.userId = userId;
        this.companyName = companyName;
        this.position = position;
        this.interviewDate = interviewDate;
        this.tension = tension;
        this.memo = memo;
    }

    public void addQuestion(InterviewQuestion question) {
        questions.add(question);
        question.setInterview(this);
    }
}
