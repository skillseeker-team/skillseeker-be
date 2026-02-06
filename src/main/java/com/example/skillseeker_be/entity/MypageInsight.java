package com.example.skillseeker_be.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mypage_insights")
@Getter
@NoArgsConstructor
public class MypageInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @Column(name = "summary_hash", nullable = false, length = 64)
    private String summaryHash;

    @Setter
    @Column(name = "insights_json", columnDefinition = "JSON", nullable = false)
    private String insightsJson;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public MypageInsight(Long userId, String summaryHash, String insightsJson) {
        this.userId = userId;
        this.summaryHash = summaryHash;
        this.insightsJson = insightsJson;
    }
}
