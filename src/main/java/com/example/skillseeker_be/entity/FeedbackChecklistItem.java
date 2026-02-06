package com.example.skillseeker_be.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_checklist_items")
@Getter
@NoArgsConstructor
public class FeedbackChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private FeedbackReport report;

    @Setter
    @Column(name = "checklist_template_id", nullable = false, length = 50)
    private String checklistTemplateId;

    @Setter
    @Column(columnDefinition = "JSON")
    private String vars;

    @Setter
    @Column(nullable = false, length = 20)
    private String status = "TODO";

    @Setter
    @Column(name = "rendered_text", columnDefinition = "TEXT")
    private String renderedText;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
