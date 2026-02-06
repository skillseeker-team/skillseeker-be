package com.example.skillseeker_be.service;

import com.example.skillseeker_be.dto.MypageInsightsResponse;
import com.example.skillseeker_be.dto.MypageNarrativeResponse;
import com.example.skillseeker_be.dto.MypageSummaryResponse;
import com.example.skillseeker_be.entity.MypageInsight;
import com.example.skillseeker_be.llm.GeminiLlmClient;
import com.example.skillseeker_be.registry.WeaknessTagRegistry;
import com.example.skillseeker_be.repository.MypageInsightRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MypageService {

    private static final Long DEMO_USER_ID = 1L;

    private final EntityManager em;
    private final WeaknessTagRegistry weaknessTagRegistry;
    private final MypageInsightRepository insightRepository;
    private final GeminiLlmClient geminiClient;
    private final ObjectMapper objectMapper;

    public MypageSummaryResponse getSummary() {
        return MypageSummaryResponse.builder()
                .topMistakes(getTopMistakes())
                .topQuestionsByCategory(getTopQuestionsByCategory())
                .avgScore(getAvgScore())
                .checklistTop(getChecklistTop())
                .checklistIncompleteCount(getChecklistIncompleteCount())
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<MypageSummaryResponse.TopMistake> getTopMistakes() {
        String sql = """
                SELECT t.tag, COUNT(*) as cnt
                FROM feedback_weakness_tags t
                JOIN feedback_reports r ON t.report_id = r.id
                JOIN interviews i ON r.interview_id = i.id
                WHERE i.user_id = :userId AND r.status = 'DONE'
                GROUP BY t.tag
                ORDER BY cnt DESC
                LIMIT 3
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter("userId", DEMO_USER_ID);
        List<Object[]> rows = query.getResultList();

        return rows.stream().map(row -> {
            String tag = (String) row[0];
            long count = ((Number) row[1]).longValue();
            WeaknessTagRegistry.TagMeta meta = weaknessTagRegistry.get(tag);
            return MypageSummaryResponse.TopMistake.builder()
                    .tag(tag)
                    .count(count)
                    .label(meta != null ? meta.label() : tag)
                    .description(meta != null ? meta.description() : "")
                    .build();
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<MypageSummaryResponse.QuestionStat>> getTopQuestionsByCategory() {
        String sql = """
                SELECT category, question_key, cnt
                FROM (
                    SELECT q.category, q.question_key, COUNT(*) as cnt,
                           ROW_NUMBER() OVER (PARTITION BY q.category ORDER BY COUNT(*) DESC) as rn
                    FROM interview_questions q
                    JOIN interviews i ON q.interview_id = i.id
                    WHERE i.user_id = :userId AND q.category IS NOT NULL AND q.question_key IS NOT NULL
                    GROUP BY q.category, q.question_key
                ) ranked
                WHERE rn <= 5
                ORDER BY category, cnt DESC
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter("userId", DEMO_USER_ID);
        List<Object[]> rows = query.getResultList();

        Map<String, List<MypageSummaryResponse.QuestionStat>> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            String questionKey = (String) row[1];
            long count = ((Number) row[2]).longValue();
            result.computeIfAbsent(category, k -> new ArrayList<>())
                    .add(MypageSummaryResponse.QuestionStat.builder()
                            .questionKey(questionKey)
                            .count(count)
                            .build());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private MypageSummaryResponse.AvgScore getAvgScore() {
        String sql = """
                SELECT AVG(CAST(i.tension AS DECIMAL(5,2))) as avg5
                FROM (
                    SELECT tension FROM interviews
                    WHERE user_id = :userId AND tension IS NOT NULL
                    ORDER BY interview_date DESC
                    LIMIT 5
                ) i
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter("userId", DEMO_USER_ID);
        Object result = query.getSingleResult();
        Double avg = result != null ? ((Number) result).doubleValue() : null;

        return MypageSummaryResponse.AvgScore.builder()
                .type("tension")
                .avg5(avg)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<MypageSummaryResponse.ChecklistStat> getChecklistTop() {
        String sql = """
                SELECT c.checklist_template_id, COUNT(*) as cnt
                FROM feedback_checklist_items c
                JOIN feedback_reports r ON c.report_id = r.id
                JOIN interviews i ON r.interview_id = i.id
                WHERE i.user_id = :userId
                GROUP BY c.checklist_template_id
                ORDER BY cnt DESC
                LIMIT 10
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter("userId", DEMO_USER_ID);
        List<Object[]> rows = query.getResultList();

        return rows.stream().map(row ->
                MypageSummaryResponse.ChecklistStat.builder()
                        .checklistId((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build()
        ).toList();
    }

    private long getChecklistIncompleteCount() {
        String sql = """
                SELECT COUNT(*)
                FROM feedback_checklist_items c
                JOIN feedback_reports r ON c.report_id = r.id
                JOIN interviews i ON r.interview_id = i.id
                WHERE i.user_id = :userId AND c.status = 'TODO'
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter("userId", DEMO_USER_ID);
        return ((Number) query.getSingleResult()).longValue();
    }

    public MypageNarrativeResponse getNarrative() {
        MypageSummaryResponse summary = getSummary();
        List<String> lines = new ArrayList<>();

        // Line 1: interview count
        String countSql = "SELECT COUNT(*) FROM interviews WHERE user_id = :userId";
        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("userId", DEMO_USER_ID);
        long interviewCount = ((Number) countQuery.getSingleResult()).longValue();
        lines.add("지금까지 총 " + interviewCount + "회의 면접을 기록했습니다.");

        // Line 2: top mistake
        if (!summary.getTopMistakes().isEmpty()) {
            MypageSummaryResponse.TopMistake top = summary.getTopMistakes().get(0);
            lines.add("가장 자주 나타나는 약점은 '" + top.getLabel() + "'(" + top.getCount() + "회)입니다.");
        }

        // Line 3: avg score
        if (summary.getAvgScore().getAvg5() != null) {
            lines.add("최근 5회 면접의 평균 긴장도는 " + String.format("%.1f", summary.getAvgScore().getAvg5()) + "점입니다.");
        }

        // Line 4: checklist progress
        long total = summary.getChecklistTop().stream().mapToLong(MypageSummaryResponse.ChecklistStat::getCount).sum();
        long incomplete = summary.getChecklistIncompleteCount();
        if (total > 0) {
            long done = total - incomplete;
            lines.add("체크리스트 진행률: " + done + "/" + total + " 항목 완료 (" + (total > 0 ? (done * 100 / total) : 0) + "%)");
        }

        // Line 5: top category
        if (!summary.getTopQuestionsByCategory().isEmpty()) {
            String topCategory = summary.getTopQuestionsByCategory().keySet().iterator().next();
            lines.add("가장 많이 출제된 카테고리는 '" + topCategory + "'입니다.");
        }

        if (lines.size() < 3) {
            lines.add("면접 데이터를 더 추가하면 상세한 분석을 받을 수 있습니다.");
        }

        return MypageNarrativeResponse.builder().narratives(lines).build();
    }

    @Transactional
    public MypageInsightsResponse getInsights() {
        MypageSummaryResponse summary = getSummary();
        String summaryJson;
        try {
            summaryJson = objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            summaryJson = "{}";
        }
        String summaryHash = sha256(summaryJson);

        // Check cache
        Optional<MypageInsight> cached = insightRepository.findByUserIdAndSummaryHash(DEMO_USER_ID, summaryHash);
        if (cached.isPresent()) {
            return parseInsightsJson(cached.get().getInsightsJson(), true);
        }

        // Call LLM with summary only
        String prompt = """
                You are an interview coach. Based on the following interview statistics summary, provide actionable insights.

                SUMMARY DATA:
                %s

                Respond with ONLY valid JSON:
                {
                  "highlights": ["<insight1 in Korean>", "<insight2 in Korean>", "<insight3 in Korean>"],
                  "next_actions": ["<action1 in Korean>", "<action2 in Korean>"]
                }

                Rules:
                - highlights: 2~3 key observations
                - next_actions: 2 specific actionable recommendations
                - All text in Korean
                - No markdown, only JSON
                """.formatted(summaryJson);

        String response = geminiClient.call(prompt);
        MypageInsightsResponse result = parseInsightsJson(response, false);

        // Cache
        MypageInsight insight = new MypageInsight(DEMO_USER_ID, summaryHash, response);
        insightRepository.save(insight);

        return result;
    }

    private MypageInsightsResponse parseInsightsJson(String json, boolean isCached) {
        try {
            JsonNode root = objectMapper.readTree(json);
            List<String> highlights = new ArrayList<>();
            JsonNode hNode = root.path("highlights");
            if (hNode.isArray()) {
                hNode.forEach(n -> highlights.add(n.asText()));
            }
            List<String> actions = new ArrayList<>();
            JsonNode aNode = root.path("next_actions");
            if (aNode.isArray()) {
                aNode.forEach(n -> actions.add(n.asText()));
            }
            return MypageInsightsResponse.builder()
                    .highlights(highlights)
                    .nextActions(actions)
                    .cached(isCached)
                    .build();
        } catch (Exception e) {
            return MypageInsightsResponse.builder()
                    .highlights(List.of("분석 데이터를 처리할 수 없습니다."))
                    .nextActions(List.of("면접 데이터를 추가해주세요."))
                    .cached(false)
                    .build();
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
