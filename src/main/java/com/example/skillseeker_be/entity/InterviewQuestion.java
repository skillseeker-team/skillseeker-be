package com.example.skillseeker_be.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_questions")
@Getter
@NoArgsConstructor
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    @Setter
    private Interview interview;

    @Column(name = "question_title", nullable = false, length = 500)
    private String questionTitle;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name = "is_hardest")
    private Boolean isHardest = false;

    @Column(name = "is_best")
    private Boolean isBest = false;

    @Setter
    @Column(length = 50)
    private String category;

    @Setter
    @Column(name = "question_key", length = 100)
    private String questionKey;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InterviewQuestion(String questionTitle, String answer,
                             Boolean isHardest, Boolean isBest) {
        this.questionTitle = questionTitle;
        this.answer = answer;
        this.isHardest = isHardest != null ? isHardest : false;
        this.isBest = isBest != null ? isBest : false;
    }
}
