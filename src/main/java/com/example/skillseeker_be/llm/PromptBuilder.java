package com.example.skillseeker_be.llm;

import com.example.skillseeker_be.enums.QuestionCategory;
import com.example.skillseeker_be.enums.WeaknessTag;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    private static final String CATEGORIES = Arrays.stream(QuestionCategory.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    private static final String WEAKNESS_TAGS = Arrays.stream(WeaknessTag.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    private static final String CHECKLIST_IDS = "REVIEW_STAR, ADD_METRIC, STUDY_CONCEPT, PRACTICE_FOLLOWUP, " +
            "SIMPLIFY_ANSWER, ADD_EXAMPLE, COMPARE_TRADEOFF, DRAW_DIAGRAM, MOCK_INTERVIEW, DEEP_DIVE, " +
            "WRITE_CODE, REVIEW_PROJECT, HANDLE_PRESSURE, OWNERSHIP_STORY, CONCISE_PITCH";

    public String build(String payloadJson) {
        return """
                You are an expert interview coach. Analyze the following interview data and provide structured feedback.

                INTERVIEW DATA:
                %s

                INSTRUCTIONS:
                - Respond with ONLY valid JSON. No markdown, no explanation, no extra text.
                - Use ONLY the allowed values below. Any value outside these lists is INVALID.

                ALLOWED question categories: [%s]
                ALLOWED weakness tags: [%s]
                ALLOWED checklist template IDs: [%s]

                OUTPUT JSON SCHEMA (strict, no additional keys):
                {
                  "questions": [
                    {
                      "index": <int, 0-based index matching questionIndexMap>,
                      "category": "<one of allowed categories>",
                      "question_key": "<lower_snake_case, 3~8 tokens, stable identifier for this question>"
                    }
                  ],
                  "weakness_tags": [
                    {
                      "tag": "<one of allowed weakness tags>",
                      "reason": "<1-2 sentence reason in Korean>"
                    }
                  ],
                  "overall_summary": ["<sentence1 in Korean>", "<sentence2 in Korean>", "<sentence3 in Korean>"],
                  "improvement_one": "<most important improvement advice in Korean, 1-2 sentences>",
                  "checklist": [
                    {
                      "id": "<one of allowed checklist template IDs>",
                      "vars": { "<key>": "<value>" }
                    }
                  ]
                }

                RULES:
                1. "questions" array must have one entry per question in questionIndexMap.
                2. "question_key" must be lower_snake_case with 3~8 tokens (e.g., "tcp_three_way_handshake").
                3. "weakness_tags" should contain 1~3 tags.
                4. "overall_summary" must have exactly 3 Korean sentences.
                5. "checklist" should contain 3~5 actionable items.
                6. checklist vars must match the template placeholders (e.g., topic, concept, conceptA, conceptB).
                """.formatted(payloadJson, CATEGORIES, WEAKNESS_TAGS, CHECKLIST_IDS);
    }
}
