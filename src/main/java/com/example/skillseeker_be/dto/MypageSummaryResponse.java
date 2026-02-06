package com.example.skillseeker_be.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MypageSummaryResponse {

    private List<TopMistake> topMistakes;
    private Map<String, List<QuestionStat>> topQuestionsByCategory;
    private AvgScore avgScore;
    private List<ChecklistStat> checklistTop;
    private long checklistIncompleteCount;

    @Getter
    @Builder
    public static class TopMistake {
        private String tag;
        private long count;
        private String label;
        private String description;
    }

    @Getter
    @Builder
    public static class QuestionStat {
        private String questionKey;
        private long count;
    }

    @Getter
    @Builder
    public static class AvgScore {
        private String type;
        private Double avg5;
    }

    @Getter
    @Builder
    public static class ChecklistStat {
        private String checklistId;
        private long count;
    }
}
