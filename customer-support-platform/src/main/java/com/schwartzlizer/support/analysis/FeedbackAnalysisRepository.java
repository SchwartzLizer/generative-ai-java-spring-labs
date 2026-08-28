package com.schwartzlizer.support.analysis;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FeedbackAnalysisRepository extends JpaRepository<FeedbackAnalysis, UUID> {
    List<FeedbackAnalysis> findByFeedback_IdOrderByCreatedAtAsc(UUID feedbackId);
    FeedbackAnalysis findTopByFeedback_IdOrderByCreatedAtDesc(UUID feedbackId);
    long countBySentiment(Sentiment sentiment);
    long countByCategory(SupportCategory category);
    long countByUrgency(Urgency urgency);
}
