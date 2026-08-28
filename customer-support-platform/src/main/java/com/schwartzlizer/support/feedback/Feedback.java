package com.schwartzlizer.support.feedback;

import com.schwartzlizer.support.common.InvalidStateTransitionException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "feedback", indexes = {
    @Index(name = "idx_feedback_status", columnList = "status"),
    @Index(name = "idx_feedback_created_at", columnList = "created_at")
})
public class Feedback {
    @Id private UUID id;
    @Column(name = "customer_reference", nullable = false, length = 100) private String customerReference;
    @Column(nullable = false, length = 4000) private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private FeedbackStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private Long version;

    protected Feedback() { }
    private Feedback(UUID id, String customerReference, String message, Instant createdAt) {
        this.id = id; this.customerReference = customerReference; this.message = message;
        this.status = FeedbackStatus.NEW; this.createdAt = createdAt; this.updatedAt = createdAt;
    }
    public static Feedback create(UUID id, String customerReference, String message, Instant createdAt) {
        if (id == null || customerReference == null || customerReference.isBlank() || message == null || message.isBlank() || createdAt == null) throw new IllegalArgumentException("Feedback fields are required");
        return new Feedback(id, customerReference.trim(), message.trim(), createdAt);
    }
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
        status = next; updatedAt = changedAt;
    }
    public UUID id() { return id; }
    public String customerReference() { return customerReference; }
    public String message() { return message; }
    public FeedbackStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Long version() { return version; }
}
