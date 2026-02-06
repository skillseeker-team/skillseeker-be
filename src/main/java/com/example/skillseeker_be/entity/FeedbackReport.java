package com.example.skillseeker_be.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feedback_reports")
@Getter
@NoArgsConstructor
public class FeedbackReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    @Setter
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Setter
    @Column(name = "payload_hash", length = 64)
    private String payloadHash;

    @Setter
    @Column(name = "overall_summary", columnDefinition = "JSON")
    private String overallSummary;

    @Setter
    @Column(name = "improvement_one", columnDefinition = "TEXT")
    private String improvementOne;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackWeaknessTag> weaknessTags = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackChecklistItem> checklistItems = new ArrayList<>();
}
