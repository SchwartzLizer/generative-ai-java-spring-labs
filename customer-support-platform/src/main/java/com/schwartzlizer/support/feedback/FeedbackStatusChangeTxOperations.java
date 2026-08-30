package com.schwartzlizer.support.feedback;

import com.schwartzlizer.support.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class FeedbackStatusChangeTxOperations {
    private final FeedbackRepository feedbackRepository;
    private final Clock clock;

    public FeedbackStatusChangeTxOperations(FeedbackRepository feedbackRepository, Clock clock) {
        this.feedbackRepository = feedbackRepository;
        this.clock = clock;
    }

    @Transactional
    public FeedbackResponse changeStatus(UUID id, FeedbackStatus status) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
        feedback.changeStatus(status, Instant.now(clock));
        return FeedbackResponse.basic(feedbackRepository.save(feedback));
    }
}
