package com.example.skillseeker_be.registry;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ChecklistTemplateRegistry {

    private static final Map<String, String> TEMPLATES = new LinkedHashMap<>();

    static {
        TEMPLATES.put("REVIEW_STAR", "STAR 기법으로 '{topic}' 답변을 다시 작성해보세요.");
        TEMPLATES.put("ADD_METRIC", "'{topic}' 답변에 수치/지표를 추가하세요.");
        TEMPLATES.put("STUDY_CONCEPT", "'{concept}' 개념을 복습하고 예시를 정리하세요.");
        TEMPLATES.put("PRACTICE_FOLLOWUP", "'{topic}'에 대한 꼬리질문 3개를 만들어 연습하세요.");
        TEMPLATES.put("SIMPLIFY_ANSWER", "'{topic}' 답변을 30초 내로 설명할 수 있게 요약하세요.");
        TEMPLATES.put("ADD_EXAMPLE", "'{topic}' 답변에 구체적인 경험 사례를 추가하세요.");
        TEMPLATES.put("COMPARE_TRADEOFF", "'{conceptA}' vs '{conceptB}' 트레이드오프를 정리하세요.");
        TEMPLATES.put("DRAW_DIAGRAM", "'{topic}' 구조를 다이어그램으로 그려보세요.");
        TEMPLATES.put("MOCK_INTERVIEW", "'{topic}' 주제로 모의 면접을 진행해보세요.");
        TEMPLATES.put("DEEP_DIVE", "'{concept}'의 내부 동작 원리를 3단계로 설명해보세요.");
        TEMPLATES.put("WRITE_CODE", "'{topic}' 관련 간단한 코드 예제를 작성해보세요.");
        TEMPLATES.put("REVIEW_PROJECT", "프로젝트에서 '{topic}' 관련 경험을 구체적으로 정리하세요.");
        TEMPLATES.put("HANDLE_PRESSURE", "압박 질문 '{topic}'에 대한 침착한 답변을 준비하세요.");
        TEMPLATES.put("OWNERSHIP_STORY", "'{topic}'에서 본인의 주도적 역할을 강조하는 답변을 작성하세요.");
        TEMPLATES.put("CONCISE_PITCH", "'{topic}'에 대해 1분 엘리베이터 피치를 준비하세요.");
    }

    public String render(String templateId, Map<String, String> vars) {
        String template = TEMPLATES.get(templateId);
        if (template == null) {
            return "[Unknown template: " + templateId + "]";
        }
        String result = template;
        if (vars != null) {
            for (Map.Entry<String, String> entry : vars.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return result;
    }

    public boolean exists(String templateId) {
        return TEMPLATES.containsKey(templateId);
    }

    public Map<String, String> getAllTemplates() {
        return Map.copyOf(TEMPLATES);
    }
}
