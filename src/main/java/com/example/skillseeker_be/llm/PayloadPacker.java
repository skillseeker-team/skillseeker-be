package com.example.skillseeker_be.llm;

import com.example.skillseeker_be.entity.Interview;
import com.example.skillseeker_be.entity.InterviewQuestion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PayloadPacker {

    private static final int MEMO_MAX = 500;
    private static final int ANSWER_MAX = 800;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{2,4}[\\-.]\\d{3,4}[\\-.]\\d{4}");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s]+");

    private final ObjectMapper objectMapper;

    public PackResult pack(Interview interview) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("company", interview.getCompanyName());
        root.put("position", interview.getPosition());
        root.put("interviewDate", interview.getInterviewDate().toString());

        // Prefer tension over atmosphere
        if (interview.getTension() != null) {
            root.put("tension", interview.getTension());
        }

        // Memo with limit
        if (interview.getMemo() != null) {
            root.put("memo", truncate(maskPii(interview.getMemo()), MEMO_MAX));
        }

        // Separate questions by role
        InterviewQuestion hardest = null;
        InterviewQuestion best = null;
        ArrayNode restTitles = objectMapper.createArrayNode();

        for (int i = 0; i < interview.getQuestions().size(); i++) {
            InterviewQuestion q = interview.getQuestions().get(i);
            if (Boolean.TRUE.equals(q.getIsHardest()) && hardest == null) {
                hardest = q;
            } else if (Boolean.TRUE.equals(q.getIsBest()) && best == null) {
                best = q;
            } else {
                ObjectNode titleNode = objectMapper.createObjectNode();
                titleNode.put("index", i);
                titleNode.put("title", maskPii(q.getQuestionTitle()));
                restTitles.add(titleNode);
            }
        }

        // Hardest Q/A (required)
        if (hardest != null) {
            ObjectNode hardestNode = objectMapper.createObjectNode();
            hardestNode.put("questionTitle", maskPii(hardest.getQuestionTitle()));
            hardestNode.put("answer", truncate(maskPii(hardest.getAnswer() != null ? hardest.getAnswer() : ""), ANSWER_MAX));
            root.set("hardestQuestion", hardestNode);
        }

        // Best Q/A (optional)
        if (best != null) {
            ObjectNode bestNode = objectMapper.createObjectNode();
            bestNode.put("questionTitle", maskPii(best.getQuestionTitle()));
            bestNode.put("answer", truncate(maskPii(best.getAnswer() != null ? best.getAnswer() : ""), ANSWER_MAX));
            root.set("bestQuestion", bestNode);
        }

        // Rest: titles only
        root.set("otherQuestions", restTitles);

        // Build question index mapping
        ArrayNode indexMap = objectMapper.createArrayNode();
        for (int i = 0; i < interview.getQuestions().size(); i++) {
            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("index", i);
            entry.put("questionId", interview.getQuestions().get(i).getId());
            entry.put("title", interview.getQuestions().get(i).getQuestionTitle());
            indexMap.add(entry);
        }
        root.set("questionIndexMap", indexMap);

        String json = root.toString();
        String hash = sha256(json);

        return new PackResult(json, hash);
    }

    private String maskPii(String text) {
        if (text == null) return "";
        text = EMAIL_PATTERN.matcher(text).replaceAll("[EMAIL]");
        text = PHONE_PATTERN.matcher(text).replaceAll("[PHONE]");
        text = URL_PATTERN.matcher(text).replaceAll("[URL]");
        return text;
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public record PackResult(String payloadJson, String payloadHash) {}
}
