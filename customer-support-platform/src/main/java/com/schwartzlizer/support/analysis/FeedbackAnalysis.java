package com.schwartzlizer.support.analysis;

import com.schwartzlizer.support.feedback.Feedback;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback_analysis", indexes = {
    @Index(name = "idx_analysis_feedback", columnList = "feedback_id"),
    @Index(name = "idx_analysis_sentiment", columnList = "sentiment"),
    @Index(name = "idx_analysis_category", columnList = "category"),
    @Index(name = "idx_analysis_urgency", columnList = "urgency")
})
public class FeedbackAnalysis {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "feedback_id", nullable = false) private Feedback feedback;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Sentiment sentiment;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private SupportCategory category;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Urgency urgency;
    @Column(name = "recommended_action", nullable = false, length = 1000) private String recommendedAction;
    @Column(nullable = false, length = 50) private String provider;
    @Column(nullable = false, length = 100) private String model;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected FeedbackAnalysis() { }
    private FeedbackAnalysis(UUID id, Feedback feedback, Sentiment sentiment, SupportCategory category, Urgency urgency, String action, String provider, String model, Instant createdAt) {
        this.id = id; this.feedback = feedback; this.sentiment = sentiment; this.category = category; this.urgency = urgency; this.recommendedAction = action; this.provider = provider; this.model = model; this.createdAt = createdAt;
    }
    public static FeedbackAnalysis create(UUID id, Feedback feedback, Sentiment sentiment, SupportCategory category, Urgency urgency, String action, String provider, String model, Instant createdAt) {
        if (id == null || feedback == null || sentiment == null || category == null || urgency == null || action == null || action.isBlank() || provider == null || provider.isBlank() || model == null || model.isBlank() || createdAt == null) throw new IllegalArgumentException("Analysis fields are required");
        return new FeedbackAnalysis(id, feedback, sentiment, category, urgency, action.trim(), provider.trim(), model.trim(), createdAt);
    }
    public UUID id() { return id; } public Feedback feedback() { return feedback; } public Sentiment sentiment() { return sentiment; } public SupportCategory category() { return category; } public Urgency urgency() { return urgency; } public String recommendedAction() { return recommendedAction; } public String provider() { return provider; } public String model() { return model; } public Instant createdAt() { return createdAt; }
}
