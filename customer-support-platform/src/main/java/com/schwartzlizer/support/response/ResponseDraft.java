package com.schwartzlizer.support.response;

import com.schwartzlizer.support.common.InvalidStateTransitionException;
import com.schwartzlizer.support.feedback.Feedback;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for one AI-generated reply proposal attached to a {@link Feedback}, created in decision
 * {@code PENDING}.
 *
 * <p>Decide-once is enforced in two complementary layers:
 * <ul>
 * <li>Within a transaction, {@link #approve(Instant)} and {@link #reject(Instant)} reject any second decision on
 * an already-decided draft by throwing {@link InvalidStateTransitionException}. Once decided, the draft is
 * immutable and {@code decidedAt} is set.</li>
 * <li>Across transactions, the {@code @Version} column makes the update conditional on the version the
 * transaction read. If two transactions each load the same {@code PENDING} draft and both decide it, the first
 * to commit wins and the second fails with Spring's
 * {@link org.springframework.orm.ObjectOptimisticLockingFailureException} at commit time.</li>
 * </ul>
 *
 * <p>The in-memory check alone cannot enforce decide-once across transactions, because it cannot see another
 * transaction's uncommitted work; the two mechanisms are complementary, not redundant. This matches
 * {@link Feedback}, which carries the same {@code @Version} protection.
 *
 * <p>{@code provider} and {@code model} record which AI provider produced the content, so a draft stays
 * auditable after the provider changes. The {@code @ManyToOne} association to {@code Feedback} is lazy; touching
 * {@link #feedback()} outside an open transaction triggers lazy initialisation.
 */
@Entity
@Table(name = "response_draft", indexes = { @Index(name = "idx_draft_feedback", columnList = "feedback_id"), @Index(name = "idx_draft_decision", columnList = "decision") })
public class ResponseDraft {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;
    @Column(nullable = false, length = 4000)
    private String content;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DraftDecision decision;
    @Column(nullable = false, length = 50)
    private String provider;
    @Column(nullable = false, length = 100)
    private String model;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "decided_at")
    private Instant decidedAt;
    @Version
    private Long version;
    protected ResponseDraft() { }
    private ResponseDraft(UUID id, Feedback feedback, String content, String provider, String model, Instant createdAt) {
        this.id=id;
        this.feedback=feedback;
        this.content=content;
        this.decision=DraftDecision.PENDING;
        this.provider=provider;
        this.model=model;
        this.createdAt=createdAt;
    }
    /**
     * Creates a pending draft for the given feedback.
     *
     * <p>{@code content}, {@code provider} and {@code model} are trimmed; decision starts at {@code PENDING} and
     * {@code decidedAt} is null until a decision is recorded. The version field stays null until the entity is
     * first persisted; Hibernate seeds it on insert.
     *
     * @param id caller-assigned identifier
     * @param feedback the feedback this draft answers
     * @param content the generated reply text, max 4000 chars
     * @param provider identifier of the AI provider that produced the content (for example "demo" or "gemini")
     * @param model model name reported by that provider
     * @param createdAt creation timestamp from the application {@code Clock}
     * @return the new instance in {@code PENDING} decision, not yet persisted
     * @throws IllegalArgumentException if any argument is null, or content, provider or model is blank
     */
    public static ResponseDraft create(UUID id, Feedback feedback, String content, String provider, String model, Instant createdAt) {
        if (id == null || feedback == null || content == null || content.isBlank() || provider == null || provider.isBlank() || model == null || model.isBlank() || createdAt == null) throw new IllegalArgumentException("Draft fields are required");
        return new ResponseDraft(id, feedback, content.trim(), provider.trim(), model.trim(), createdAt);
    }
    /**
     * Records an approval decision on a pending draft.
     *
     * @param at the decision timestamp, stored as {@code decidedAt}
     * @throws InvalidStateTransitionException if the draft has already been approved or rejected
     * @throws IllegalArgumentException if at is null
     */
    public void approve(Instant at) { decide(DraftDecision.APPROVED, at); }
    /**
     * Records a rejection decision on a pending draft.
     *
     * @param at the decision timestamp, stored as {@code decidedAt}
     * @throws InvalidStateTransitionException if the draft has already been approved or rejected
     * @throws IllegalArgumentException if at is null
     */
    public void reject(Instant at) { decide(DraftDecision.REJECTED, at); }
    private void decide(DraftDecision next, Instant at) {
        if (decision != DraftDecision.PENDING) throw new InvalidStateTransitionException("Draft has already been decided");
        if (at == null) throw new IllegalArgumentException("Decision timestamp is required");
        decision=next;
        decidedAt=at;
    }
    public UUID id() { return id; }
    public Feedback feedback() { return feedback; }
    public String content() { return content; }
    public DraftDecision decision() { return decision; }
    public String provider() { return provider; }
    public String model() { return model; }
    public Instant createdAt() { return createdAt; }
    public Instant decidedAt() { return decidedAt; }
    public Long version() { return version; }
}
