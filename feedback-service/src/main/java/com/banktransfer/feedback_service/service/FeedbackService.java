package com.banktransfer.feedback_service.service;

import com.banktransfer.feedback_service.dto.FeedbackRequest;
import com.banktransfer.feedback_service.dto.FeedbackResponse;
import com.banktransfer.feedback_service.mapper.FeedbackMapper;
import com.banktransfer.feedback_service.model.Feedback;
import com.banktransfer.feedback_service.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;

    public FeedbackResponse createFeedback(FeedbackRequest request) {
        Feedback feedback = feedbackMapper.toEntity(request);
        Feedback saved = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(saved);
    }

    public List<FeedbackResponse> getFeedbacksByUserId(Long userId) {
        return feedbackRepository.findByUserId(userId).stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<FeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }
}