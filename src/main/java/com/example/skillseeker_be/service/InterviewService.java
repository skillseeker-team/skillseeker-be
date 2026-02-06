package com.example.skillseeker_be.service;

import com.example.skillseeker_be.dto.InterviewCreateRequest;
import com.example.skillseeker_be.dto.InterviewListResponse;
import com.example.skillseeker_be.dto.InterviewResponse;
import com.example.skillseeker_be.entity.Interview;
import com.example.skillseeker_be.entity.InterviewQuestion;
import com.example.skillseeker_be.exception.BadRequestException;
import com.example.skillseeker_be.exception.NotFoundException;
import com.example.skillseeker_be.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {

    private static final Long DEMO_USER_ID = 1L;

    private final InterviewRepository interviewRepository;

    @Transactional
    public InterviewResponse create(InterviewCreateRequest request) {
        validateHardestQuestion(request.getQuestions());

        // Resolve tension: if both provided, keep only tensionChangeScore
        String tension = null;
        if (request.getTensionChangeScore() != null) {
            tension = String.valueOf(request.getTensionChangeScore());
        } else if (request.getAtmosphereScore() != null) {
            tension = String.valueOf(request.getAtmosphereScore());
        }

        Interview interview = Interview.builder()
                .userId(DEMO_USER_ID)
                .companyName(request.getCompany())
                .position(request.getRole())
                .interviewDate(request.getInterviewDate())
                .tension(tension)
                .memo(request.getMemo())
                .build();

        for (InterviewCreateRequest.QuestionRequest qr : request.getQuestions()) {
            InterviewQuestion question = InterviewQuestion.builder()
                    .questionTitle(qr.getQuestionText())
                    .answer(qr.getAnswerText())
                    .isHardest(qr.getIsHardest())
                    .isBest(qr.getIsBest())
                    .build();
            interview.addQuestion(question);
        }

        Interview saved = interviewRepository.save(interview);
        return InterviewResponse.from(saved);
    }

    public List<InterviewListResponse> list() {
        return interviewRepository.findByUserIdOrderByCreatedAtDesc(DEMO_USER_ID)
                .stream()
                .map(InterviewListResponse::from)
                .toList();
    }

    public InterviewResponse getById(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Interview not found: " + id));
        return InterviewResponse.from(interview);
    }

    @Transactional
    public void delete(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Interview not found: " + id));
        interviewRepository.delete(interview);
    }

    private void validateHardestQuestion(List<InterviewCreateRequest.QuestionRequest> questions) {
        long hardestCount = questions.stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsHardest()))
                .count();
        if (hardestCount != 1) {
            throw new BadRequestException("Exactly one question must be marked as hardest (found: " + hardestCount + ")");
        }
    }
}
