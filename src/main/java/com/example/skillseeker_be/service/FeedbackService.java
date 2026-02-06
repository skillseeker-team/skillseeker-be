package com.example.skillseeker_be.service;

import com.example.skillseeker_be.dto.FeedbackResponse;
import com.example.skillseeker_be.entity.*;
import com.example.skillseeker_be.exception.BadRequestException;
import com.example.skillseeker_be.exception.NotFoundException;
import com.example.skillseeker_be.llm.*;
import com.example.skillseeker_be.registry.ChecklistTemplateRegistry;
import com.example.skillseeker_be.registry.WeaknessTagRegistry;
import com.example.skillseeker_be.repository.FeedbackChecklistItemRepository;
import com.example.skillseeker_be.repository.FeedbackReportRepository;
import com.example.skillseeker_be.repository.FeedbackWeaknessTagRepository;
import com.example.skillseeker_be.repository.InterviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FeedbackService {

    private final InterviewRepository interviewRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final FeedbackWeaknessTagRepository weaknessTagRepository;
    private final FeedbackChecklistItemRepository checklistItemRepository;
    private final PayloadPacker payloadPacker;
    private final PromptBuilder promptBuilder;
    private final GeminiLlmClient geminiClient;
    private final LlmResponseParser responseParser;
    private final ChecklistTemplateRegistry checklistRegistry;
    private final WeaknessTagRegistry weaknessTagRegistry;
    private final ObjectMapper objectMapper;

    @Transactional
    public FeedbackResponse generateAiReport(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new NotFoundException("Interview not found: " + interviewId));

        // Pack payload and compute hash
        PayloadPacker.PackResult packResult = payloadPacker.pack(interview);
        String payloadHash = packResult.payloadHash();

        // Check cache: if DONE report with same hash exists, return it
        FeedbackReport existing = interview.getFeedbackReport();
        if (existing != null && "DONE".equals(existing.getStatus())
                && payloadHash.equals(existing.getPayloadHash())) {
            log.info("Returning cached feedback for interview={}, hash={}", interviewId, payloadHash);
            return buildResponse(existing);
        }

        // Create or reset report
        FeedbackReport report;
        if (existing != null) {
            report = existing;
            // Clear old data for retry
            weaknessTagRepository.deleteAll(report.getWeaknessTags());
            report.getWeaknessTags().clear();
            checklistItemRepository.deleteAll(report.getChecklistItems());
            report.getChecklistItems().clear();
        } else {
            report = new FeedbackReport();
            report.setInterview(interview);
        }
        report.setStatus("PROCESSING");
        report.setPayloadHash(payloadHash);
        report = feedbackReportRepository.save(report);

        // Call LLM
        String prompt = promptBuilder.build(packResult.payloadJson());
        String llmResponse;
        try {
            llmResponse = geminiClient.call(prompt);
        } catch (Exception e) {
            log.error("LLM call failed for interview={}: {}", interviewId, e.getMessage());
            report.setStatus("FAILED");
            feedbackReportRepository.save(report);
            throw new BadRequestException("AI feedback generation failed. You can retry.");
        }

        // Parse and validate
        LlmResponseParser.ParsedFeedback parsed;
        try {
            parsed = responseParser.parse(llmResponse);
        } catch (IllegalArgumentException e) {
            log.error("LLM response validation failed for interview={}: {}", interviewId, e.getMessage());
            report.setStatus("FAILED");
            feedbackReportRepository.save(report);
            throw new BadRequestException("AI feedback output was invalid. You can retry. Reason: " + e.getMessage());
        }

        // Persist report fields
        try {
            report.setOverallSummary(objectMapper.writeValueAsString(parsed.overallSummary));
        } catch (JsonProcessingException e) {
            report.setOverallSummary("[]");
        }
        report.setImprovementOne(parsed.improvementOne);
        report.setStatus("DONE");

        // Update question categories and keys
        List<InterviewQuestion> questions = interview.getQuestions();
        for (LlmResponseParser.ParsedQuestion pq : parsed.questions) {
            if (pq.index() >= 0 && pq.index() < questions.size()) {
                InterviewQuestion q = questions.get(pq.index());
                q.setCategory(pq.category());
                q.setQuestionKey(pq.questionKey());
            }
        }

        // Persist weakness tags
        for (LlmResponseParser.ParsedWeaknessTag pt : parsed.weaknessTags) {
            FeedbackWeaknessTag tag = new FeedbackWeaknessTag();
            tag.setReport(report);
            tag.setTag(pt.tag());
            tag.setReason(pt.reason());
            report.getWeaknessTags().add(tag);
        }

        // Persist checklist items
        for (LlmResponseParser.ParsedChecklistItem pc : parsed.checklist) {
            FeedbackChecklistItem item = new FeedbackChecklistItem();
            item.setReport(report);
            item.setChecklistTemplateId(pc.id());
            try {
                item.setVars(objectMapper.writeValueAsString(pc.vars()));
            } catch (JsonProcessingException e) {
                item.setVars("{}");
            }
            item.setRenderedText(checklistRegistry.render(pc.id(), pc.vars()));
            item.setStatus("TODO");
            report.getChecklistItems().add(item);
        }

        feedbackReportRepository.save(report);
        log.info("Feedback generated successfully for interview={}, hash={}", interviewId, payloadHash);

        return buildResponse(report);
    }

    public FeedbackResponse getFeedback(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new NotFoundException("Interview not found: " + interviewId));

        FeedbackReport report = interview.getFeedbackReport();
        if (report == null) {
            throw new NotFoundException("Feedback not found for interview: " + interviewId);
        }

        return buildResponse(report);
    }

    private FeedbackResponse buildResponse(FeedbackReport report) {
        List<String> summaryList;
        try {
            String[] arr = objectMapper.readValue(
                    report.getOverallSummary() != null ? report.getOverallSummary() : "[]",
                    String[].class);
            summaryList = Arrays.asList(arr);
        } catch (JsonProcessingException e) {
            summaryList = List.of();
        }

        List<FeedbackResponse.WeaknessTagResponse> tagResponses = report.getWeaknessTags().stream()
                .map(t -> {
                    WeaknessTagRegistry.TagMeta meta = weaknessTagRegistry.get(t.getTag());
                    return FeedbackResponse.WeaknessTagResponse.builder()
                            .tag(t.getTag())
                            .label(meta != null ? meta.label() : t.getTag())
                            .description(meta != null ? meta.description() : "")
                            .reason(t.getReason())
                            .build();
                })
                .toList();

        List<FeedbackResponse.ChecklistItemResponse> checklistResponses = report.getChecklistItems().stream()
                .map(c -> FeedbackResponse.ChecklistItemResponse.builder()
                        .id(c.getId())
                        .templateId(c.getChecklistTemplateId())
                        .renderedText(c.getRenderedText())
                        .status(c.getStatus())
                        .build())
                .toList();

        return FeedbackResponse.builder()
                .id(report.getId())
                .interviewId(report.getInterview().getId())
                .status(report.getStatus())
                .overallSummary(summaryList)
                .improvementOne(report.getImprovementOne())
                .weaknessTags(tagResponses)
                .checklistItems(checklistResponses)
                .createdAt(report.getCreatedAt())
                .build();
    }
}
