package com.schwartzlizer.support.feedback;

import com.schwartzlizer.support.common.InvalidStateTransitionException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate root for a customer feedback item and the single owner of its status state machine; status may only
 * change through {@link #changeStatus(FeedbackStatus, Instant)}, never by direct assignment.
 *
 * <p>Mapped by JPA to the {@code feedback} table and carries an {@code @Version} field, so concurrent status
 * changes on the same row fail the losing transaction with Spring's
 * {@link org.springframework.dao.OptimisticLockingFailureException}.
 *
 * <p>Timestamps are supplied by the caller, sourced from an injected {@code Clock} rather than read from the
 * system clock, which is what makes the behaviour testable.
 *
 * <p>The protected no-arg constructor exists only for JPA.
 */
@Entity
@Table(name = "feedback", indexes = {
    @Index(name = "idx_feedback_status", columnList = "status"),
    @Index(name = "idx_feedback_created_at", columnList = "created_at")
})
public class Feedback {
    @Id
    private UUID id;
    @Column(name = "customer_reference", nullable = false, length = 100)
    private String customerReference;
    @Column(nullable = false, length = 4000)
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FeedbackStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private Long version;

    protected Feedback() { }
    private Feedback(UUID id, String customerReference, String message, Instant createdAt) {
        this.id = id;
        this.customerReference = customerReference;
        this.message = message;
        this.status = FeedbackStatus.NEW;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
    /**
     * Creates a new feedback item in status {@code NEW}.
     *
     * <p>{@code customerReference} and {@code message} are trimmed; {@code createdAt} is used for both
     * {@code createdAt} and {@code updatedAt}. The version field stays null until the entity is first persisted.
     *
     * @param id identifier assigned by the caller; the service supplies it from a {@code Supplier<UUID>}, so
     *           tests can make it deterministic
     * @param customerReference caller-supplied reference for the customer, max 100 chars
     * @param message the raw feedback text, max 4000 chars
     * @param createdAt creation timestamp, taken from the application {@code Clock}
     * @return the new instance, status {@code NEW}, not yet persisted
     * @throws IllegalArgumentException if any argument is null, or customerReference or message is blank
     */
    public static Feedback create(UUID id, String customerReference, String message, Instant createdAt) {
        if (id == null || customerReference == null || customerReference.isBlank() || message == null || message.isBlank() || createdAt == null) throw new IllegalArgumentException("Feedback fields are required");
        return new Feedback(id, customerReference.trim(), message.trim(), createdAt);
    }
    /**
     * Applies a status transition and refreshes the update timestamp.
     *
     * <p>The legal transitions are:
     * <ul>
     * <li>{@code NEW} -> {@code ANALYZED} or {@code IN_PROGRESS}</li>
     * <li>{@code ANALYZED} -> {@code IN_PROGRESS}</li>
     * <li>{@code IN_PROGRESS} -> {@code RESOLVED}</li>
     * <li>{@code RESOLVED} -> {@code IN_PROGRESS} or {@code CLOSED}</li>
     * <li>{@code CLOSED} -> terminal, no transition permitted</li>
     * </ul>
     *
     * <p>A status cannot transition to itself.
     *
     * @param next the requested target status
     * @param changedAt timestamp stored as {@code updatedAt}
     * @throws IllegalArgumentException if either argument is null
     * @throws InvalidStateTransitionException if the transition is not in the table above
     */
    public void changeStatus(FeedbackStatus next, Instant changedAt) {
        if (next == null || changedAt == null) throw new IllegalArgumentException("Status and timestamp are required");
        Set<FeedbackStatus> allowed = switch (status) {
            case NEW -> EnumSet.of(FeedbackStatus.ANALYZED, FeedbackStatus.IN_PROGRESS);
            case ANALYZED -> EnumSet.of(FeedbackStatus.IN_PROGRESS);
            case IN_PROGRESS -> EnumSet.of(FeedbackStatus.RESOLVED);
            case RESOLVED -> EnumSet.of(FeedbackStatus.IN_PROGRESS, FeedbackStatus.CLOSED);
            case CLOSED -> EnumSet.noneOf(FeedbackStatus.class);
        };
        if (!allowed.contains(next)) throw new InvalidStateTransitionException("Cannot change feedback status from " + status + " to " + next);
        status = next;
        updatedAt = changedAt;
    }
    public UUID id() { return id; }
    public String customerReference() { return customerReference; }
    public String message() { return message; }
    public FeedbackStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Long version() { return version; }
}
