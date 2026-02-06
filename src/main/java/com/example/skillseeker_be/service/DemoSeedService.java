package com.example.skillseeker_be.service;

import com.example.skillseeker_be.entity.*;
import com.example.skillseeker_be.registry.ChecklistTemplateRegistry;
import com.example.skillseeker_be.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoSeedService {

    private static final Long DEMO_USER_ID = 1L;

    private final InterviewRepository interviewRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final FeedbackWeaknessTagRepository weaknessTagRepository;
    private final FeedbackChecklistItemRepository checklistItemRepository;
    private final ChecklistTemplateRegistry checklistRegistry;

    @Transactional
    public Map<String, Object> seed() {
        // Clear existing data for demo user
        List<Interview> existing = interviewRepository.findByUserIdOrderByCreatedAtDesc(DEMO_USER_ID);
        interviewRepository.deleteAll(existing);

        int interviewCount = 0;
        int questionCount = 0;
        int feedbackCount = 0;

        // Interview 1: Backend fundamentals
        Interview i1 = createInterview("네이버", "백엔드 개발자", LocalDate.now().minusDays(30), "7", "첫 면접이라 많이 긴장함");
        addQuestion(i1, "TCP 3-way handshake에 대해 설명해주세요", "SYN, SYN-ACK, ACK 과정으로 연결을 수립합니다", true, false);
        addQuestion(i1, "HTTP와 HTTPS의 차이점은?", "HTTPS는 SSL/TLS를 통해 암호화된 통신을 합니다", false, true);
        addQuestion(i1, "REST API 설계 원칙에 대해 아는대로 말해주세요", null, false, false);
        addQuestion(i1, "데이터베이스 인덱스의 동작 원리는?", "B-Tree 구조로 검색 성능을 향상시킵니다", false, false);
        interviewRepository.save(i1);
        interviewCount++;
        questionCount += 4;
        feedbackCount += createFeedbackForInterview(i1,
                List.of("전반적으로 기본 개념은 이해하고 있으나 깊이 있는 설명이 부족합니다.",
                        "네트워크 관련 답변이 표면적 수준에 머물러 있습니다.",
                        "구체적인 경험 사례를 추가하면 좋겠습니다."),
                "TCP handshake 답변에 실제 트러블슈팅 경험을 추가하세요.",
                new String[][]{{"fundamentals", "tcp_three_way_handshake"}, {"network", "http_vs_https_difference"}, {"architecture", "rest_api_design_principles"}, {"database", "index_internal_structure"}},
                new String[][]{{"depth", "TCP 핸드셰이크 설명이 교과서적 수준에 머물러 있습니다."}, {"evidence", "실무 경험을 근거로 제시하지 못했습니다."}},
                new String[][]{{"STUDY_CONCEPT", "concept", "TCP 3-way handshake"}, {"ADD_EXAMPLE", "topic", "HTTPS 적용 경험"}, {"PRACTICE_FOLLOWUP", "topic", "REST API 설계"}});

        // Interview 2: Database focused
        Interview i2 = createInterview("카카오", "서버 개발자", LocalDate.now().minusDays(22), "6", "기술 면접 위주");
        addQuestion(i2, "트랜잭션 격리 수준에 대해 설명해주세요", "READ UNCOMMITTED부터 SERIALIZABLE까지 4단계가 있습니다", true, false);
        addQuestion(i2, "JPA N+1 문제와 해결법은?", "fetch join이나 EntityGraph를 사용합니다", false, true);
        addQuestion(i2, "MySQL InnoDB의 특징은?", null, false, false);
        interviewRepository.save(i2);
        interviewCount++;
        questionCount += 3;
        feedbackCount += createFeedbackForInterview(i2,
                List.of("데이터베이스 관련 지식이 양호하나 트레이드오프 분석이 부족합니다.",
                        "JPA 관련 답변이 잘 구조화되어 있습니다.",
                        "격리 수준별 성능 영향을 구체적으로 설명하면 좋겠습니다."),
                "각 격리 수준의 트레이드오프를 실제 서비스 사례와 연결하세요.",
                new String[][]{{"database", "transaction_isolation_levels"}, {"framework", "jpa_n_plus_one_problem"}, {"database", "mysql_innodb_features"}},
                new String[][]{{"tradeoff", "격리 수준 선택의 장단점을 비교하지 못했습니다."}, {"depth", "InnoDB 내부 구조 설명이 부족합니다."}},
                new String[][]{{"COMPARE_TRADEOFF", "conceptA", "READ COMMITTED", "conceptB", "REPEATABLE READ"}, {"DEEP_DIVE", "concept", "InnoDB 클러스터 인덱스"}, {"WRITE_CODE", "topic", "JPA fetch join"}});

        // Interview 3: Architecture
        Interview i3 = createInterview("라인", "백엔드 엔지니어", LocalDate.now().minusDays(15), "8", "압박 면접 스타일");
        addQuestion(i3, "MSA와 모놀리식 아키텍처의 차이점은?", "MSA는 서비스 단위로 분리하여 독립적 배포가 가능합니다", true, false);
        addQuestion(i3, "캐시 전략에 대해 설명해주세요", "Cache-Aside, Write-Through 등의 패턴이 있습니다", false, false);
        addQuestion(i3, "본인이 가장 어려웠던 기술적 문제는?", "대용량 데이터 처리 시 메모리 이슈를 해결한 경험이 있습니다", false, true);
        addQuestion(i3, "CI/CD 파이프라인 구축 경험이 있나요?", null, false, false);
        interviewRepository.save(i3);
        interviewCount++;
        questionCount += 4;
        feedbackCount += createFeedbackForInterview(i3,
                List.of("아키텍처에 대한 이해도가 좋으나 구체적 수치가 부족합니다.",
                        "압박 질문 상황에서 침착하게 대응했습니다.",
                        "프로젝트 경험을 더 구체적으로 어필하면 좋겠습니다."),
                "MSA 답변에 실제 서비스 분리 경험과 수치를 추가하세요.",
                new String[][]{{"architecture", "msa_vs_monolith_tradeoff"}, {"performance", "cache_strategy_patterns"}, {"project", "large_data_processing_issue"}, {"devops", "cicd_pipeline_experience"}},
                new String[][]{{"structure", "MSA 답변의 구조가 산만합니다."}, {"pressure_handling", "압박 질문에서 논리적 흐름이 끊겼습니다."}},
                new String[][]{{"REVIEW_STAR", "topic", "MSA 전환 경험"}, {"ADD_METRIC", "topic", "대용량 데이터 처리"}, {"HANDLE_PRESSURE", "topic", "아키텍처 선택 근거"}});

        // Interview 4: Framework
        Interview i4 = createInterview("쿠팡", "Java 백엔드", LocalDate.now().minusDays(10), "5", null);
        addQuestion(i4, "Spring Bean 생명주기에 대해 설명해주세요", "생성-초기화-사용-소멸 단계를 거칩니다", true, false);
        addQuestion(i4, "Spring Security 인증 흐름은?", null, false, false);
        addQuestion(i4, "AOP가 무엇이고 어디에 활용하나요?", "로깅, 트랜잭션 관리 등에 활용합니다", false, true);
        interviewRepository.save(i4);
        interviewCount++;
        questionCount += 3;

        // Interview 5: Testing & DevOps
        Interview i5 = createInterview("토스", "서버 개발자", LocalDate.now().minusDays(5), "4", "편한 분위기");
        addQuestion(i5, "단위 테스트와 통합 테스트의 차이는?", "단위 테스트는 개별 모듈, 통합 테스트는 모듈 간 상호작용을 검증합니다", true, false);
        addQuestion(i5, "Docker와 Kubernetes의 차이는?", null, false, false);
        addQuestion(i5, "팀에서 코드 리뷰를 어떻게 진행하셨나요?", "PR 기반으로 최소 1명의 리뷰어 승인 후 머지했습니다", false, true);
        addQuestion(i5, "장애 대응 경험에 대해 말해주세요", "서비스 모니터링 알림 후 30분 내 핫픽스 배포한 경험이 있습니다", false, false);
        interviewRepository.save(i5);
        interviewCount++;
        questionCount += 4;

        // Interview 6: Culture fit
        Interview i6 = createInterview("배달의민족", "백엔드 개발자", LocalDate.now().minusDays(2), "3", "컬쳐핏 면접");
        addQuestion(i6, "왜 개발자가 되고 싶으셨나요?", "문제 해결 과정에서 오는 성취감이 좋아서입니다", false, false);
        addQuestion(i6, "팀 내 갈등 해결 경험은?", "서로의 의견을 경청하고 데이터 기반으로 결정했습니다", true, false);
        addQuestion(i6, "5년 후 커리어 목표는?", null, false, true);
        interviewRepository.save(i6);
        interviewCount++;
        questionCount += 3;

        log.info("Demo seed completed: {} interviews, {} questions, {} feedback reports",
                interviewCount, questionCount, feedbackCount);

        return Map.of(
                "interviews", interviewCount,
                "questions", questionCount,
                "feedbackReports", feedbackCount,
                "message", "Demo data seeded successfully"
        );
    }

    private Interview createInterview(String company, String position, LocalDate date, String tension, String memo) {
        return Interview.builder()
                .userId(DEMO_USER_ID)
                .companyName(company)
                .position(position)
                .interviewDate(date)
                .tension(tension)
                .memo(memo)
                .build();
    }

    private void addQuestion(Interview interview, String title, String answer, boolean hardest, boolean best) {
        InterviewQuestion q = InterviewQuestion.builder()
                .questionTitle(title)
                .answer(answer)
                .isHardest(hardest)
                .isBest(best)
                .build();
        interview.addQuestion(q);
    }

    private int createFeedbackForInterview(Interview interview,
                                            List<String> summaries,
                                            String improvementOne,
                                            String[][] questionMappings,
                                            String[][] weaknessTags,
                                            String[][] checklistItems) {
        // Update question categories
        for (String[] mapping : questionMappings) {
            int idx = java.util.Arrays.asList(questionMappings).indexOf(mapping);
            if (idx < interview.getQuestions().size()) {
                interview.getQuestions().get(idx).setCategory(mapping[0]);
                interview.getQuestions().get(idx).setQuestionKey(mapping[1]);
            }
        }

        FeedbackReport report = new FeedbackReport();
        report.setInterview(interview);
        report.setStatus("DONE");
        report.setPayloadHash("seed-" + interview.getCompanyName().hashCode());
        try {
            report.setOverallSummary(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(summaries));
        } catch (Exception e) {
            report.setOverallSummary("[]");
        }
        report.setImprovementOne(improvementOne);
        feedbackReportRepository.save(report);

        for (String[] tagData : weaknessTags) {
            FeedbackWeaknessTag tag = new FeedbackWeaknessTag();
            tag.setReport(report);
            tag.setTag(tagData[0]);
            tag.setReason(tagData[1]);
            weaknessTagRepository.save(tag);
        }

        for (String[] checkData : checklistItems) {
            FeedbackChecklistItem item = new FeedbackChecklistItem();
            item.setReport(report);
            item.setChecklistTemplateId(checkData[0]);
            Map<String, String> vars = new java.util.LinkedHashMap<>();
            for (int i = 1; i < checkData.length; i += 2) {
                if (i + 1 < checkData.length) {
                    vars.put(checkData[i], checkData[i + 1]);
                }
            }
            try {
                item.setVars(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(vars));
            } catch (Exception e) {
                item.setVars("{}");
            }
            item.setRenderedText(checklistRegistry.render(checkData[0], vars));
            item.setStatus("TODO");
            checklistItemRepository.save(item);
        }

        return 1;
    }
}
