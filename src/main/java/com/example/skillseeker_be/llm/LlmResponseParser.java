package com.example.skillseeker_be.llm;

import com.example.skillseeker_be.enums.QuestionCategory;
import com.example.skillseeker_be.enums.WeaknessTag;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class LlmResponseParser {

    private final ObjectMapper objectMapper;

    public ParsedFeedback parse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            ParsedFeedback feedback = new ParsedFeedback();

            // Parse questions
            JsonNode questionsNode = root.path("questions");
            if (questionsNode.isArray()) {
                for (JsonNode q : questionsNode) {
                    String category = q.path("category").asText();
                    String questionKey = q.path("question_key").asText();
                    int index = q.path("index").asInt(-1);

                    if (!QuestionCategory.isValid(category)) {
                        throw new IllegalArgumentException("Invalid category: " + category);
                    }
                    feedback.questions.add(new ParsedQuestion(index, category, questionKey));
                }
            }

            // Parse weakness_tags
            JsonNode tagsNode = root.path("weakness_tags");
            if (tagsNode.isArray()) {
                for (JsonNode t : tagsNode) {
                    String tag = t.path("tag").asText();
                    String reason = t.path("reason").asText();
                    if (!WeaknessTag.isValid(tag)) {
                        throw new IllegalArgumentException("Invalid weakness tag: " + tag);
                    }
                    feedback.weaknessTags.add(new ParsedWeaknessTag(tag, reason));
                }
            }

            // Parse overall_summary
            JsonNode summaryNode = root.path("overall_summary");
            if (summaryNode.isArray()) {
                for (JsonNode s : summaryNode) {
                    feedback.overallSummary.add(s.asText());
                }
            }
            if (feedback.overallSummary.size() != 3) {
                throw new IllegalArgumentException("overall_summary must have exactly 3 items");
            }

            // Parse improvement_one
            feedback.improvementOne = root.path("improvement_one").asText();

            // Parse checklist
            JsonNode checklistNode = root.path("checklist");
            if (checklistNode.isArray()) {
                for (JsonNode c : checklistNode) {
                    String id = c.path("id").asText();
                    Map<String, String> vars = new LinkedHashMap<>();
                    JsonNode varsNode = c.path("vars");
                    if (varsNode.isObject()) {
                        varsNode.fields().forEachRemaining(entry ->
                                vars.put(entry.getKey(), entry.getValue().asText()));
                    }
                    feedback.checklist.add(new ParsedChecklistItem(id, vars));
                }
            }

            return feedback;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }

    public static class ParsedFeedback {
        public List<ParsedQuestion> questions = new ArrayList<>();
        public List<ParsedWeaknessTag> weaknessTags = new ArrayList<>();
        public List<String> overallSummary = new ArrayList<>();
        public String improvementOne;
        public List<ParsedChecklistItem> checklist = new ArrayList<>();
    }

    public record ParsedQuestion(int index, String category, String questionKey) {}
    public record ParsedWeaknessTag(String tag, String reason) {}
    public record ParsedChecklistItem(String id, Map<String, String> vars) {}
}
