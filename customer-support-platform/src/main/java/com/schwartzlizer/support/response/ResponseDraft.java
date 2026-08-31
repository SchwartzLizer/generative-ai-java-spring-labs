package com.schwartzlizer.support.response;

import com.schwartzlizer.support.common.InvalidStateTransitionException;
import com.schwartzlizer.support.feedback.Feedback;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "response_draft", indexes = { @Index(name = "idx_draft_feedback", columnList = "feedback_id"), @Index(name = "idx_draft_decision", columnList = "decision") })
public class ResponseDraft {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "feedback_id", nullable = false) private Feedback feedback;
    @Column(nullable = false, length = 4000) private String content;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private DraftDecision decision;
    @Column(nullable = false, length = 50) private String provider;
    @Column(nullable = false, length = 100) private String model;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "decided_at") private Instant decidedAt;
    @Version private Long version;
    protected ResponseDraft() { }
    private ResponseDraft(UUID id, Feedback feedback, String content, String provider, String model, Instant createdAt) { this.id=id; this.feedback=feedback; this.content=content; this.decision=DraftDecision.PENDING; this.provider=provider; this.model=model; this.createdAt=createdAt; }
    public static ResponseDraft create(UUID id, Feedback feedback, String content, String provider, String model, Instant createdAt) {
        if (id == null || feedback == null || content == null || content.isBlank() || provider == null || provider.isBlank() || model == null || model.isBlank() || createdAt == null) throw new IllegalArgumentException("Draft fields are required");
        return new ResponseDraft(id, feedback, content.trim(), provider.trim(), model.trim(), createdAt);
    }
    public void approve(Instant at) { decide(DraftDecision.APPROVED, at); }
    public void reject(Instant at) { decide(DraftDecision.REJECTED, at); }
    private void decide(DraftDecision next, Instant at) { if (decision != DraftDecision.PENDING) throw new InvalidStateTransitionException("Draft has already been decided"); if (at == null) throw new IllegalArgumentException("Decision timestamp is required"); decision=next; decidedAt=at; }
    public UUID id() { return id; } public Feedback feedback() { return feedback; } public String content() { return content; } public DraftDecision decision() { return decision; } public String provider() { return provider; } public String model() { return model; } public Instant createdAt() { return createdAt; } public Instant decidedAt() { return decidedAt; }
    public Long version() { return version; }
}
