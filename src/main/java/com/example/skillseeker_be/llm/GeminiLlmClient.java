package com.example.skillseeker_be.llm;

import com.example.skillseeker_be.config.GeminiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiLlmClient {

    private final GeminiProperties properties;
    private final RestTemplate geminiRestTemplate;
    private final ObjectMapper objectMapper;

    public String call(String prompt) {
        if (isStubMode()) {
            log.info("LLM stub mode active, returning deterministic response");
            return stubResponse();
        }

        String url = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                properties.getBaseUrl(), properties.getModel(), properties.getApiKey());

        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode message = contents.addObject();
        message.put("role", "user");
        ArrayNode parts = message.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode genConfig = body.putObject("generationConfig");
        genConfig.put("temperature", 0.2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        log.info("Calling Gemini API (model={})", properties.getModel());
        String rawResponse = geminiRestTemplate.postForObject(url, request, String.class);

        try {
            JsonNode responseNode = objectMapper.readTree(rawResponse);
            String text = responseNode
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText();
            log.info("Gemini API call successful");
            // Strip markdown code fences if present
            text = text.strip();
            if (text.startsWith("```json")) {
                text = text.substring(7);
            } else if (text.startsWith("```")) {
                text = text.substring(3);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            return text.strip();
        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    private boolean isStubMode() {
        return properties.isStubEnabled()
                || properties.getApiKey() == null
                || properties.getApiKey().isBlank();
    }

    private String stubResponse() {
        return """
                {
                  "questions": [
                    {"index": 0, "category": "fundamentals", "question_key": "tcp_three_way_handshake"},
                    {"index": 1, "category": "database", "question_key": "index_optimization_strategy"},
                    {"index": 2, "category": "architecture", "question_key": "msa_vs_monolith_tradeoff"}
                  ],
                  "weakness_tags": [
                    {"tag": "depth", "reason": "기술적 개념 설명이 표면적 수준에 머물러 있습니다."},
                    {"tag": "structure", "reason": "답변이 체계적으로 구성되지 않아 핵심 전달이 약합니다."}
                  ],
                  "overall_summary": [
                    "전반적으로 기본 개념은 이해하고 있으나 깊이 있는 설명이 부족합니다.",
                    "답변 구조를 STAR 기법으로 개선하면 전달력이 크게 향상될 것입니다.",
                    "실무 경험을 구체적 수치와 함께 제시하면 설득력이 높아집니다."
                  ],
                  "improvement_one": "가장 시급한 개선점은 답변에 구체적인 수치와 경험 사례를 추가하는 것입니다.",
                  "checklist": [
                    {"id": "REVIEW_STAR", "vars": {"topic": "TCP 3-way handshake"}},
                    {"id": "ADD_METRIC", "vars": {"topic": "인덱스 최적화"}},
                    {"id": "STUDY_CONCEPT", "vars": {"concept": "MSA 아키텍처"}},
                    {"id": "PRACTICE_FOLLOWUP", "vars": {"topic": "데이터베이스 설계"}}
                  ]
                }
                """;
    }
}
