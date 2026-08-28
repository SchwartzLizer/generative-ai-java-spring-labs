package com.schwartzlizer.support.response;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ResponseDraftRepository extends JpaRepository<ResponseDraft, UUID> {
    List<ResponseDraft> findByFeedback_IdOrderByCreatedAtAsc(UUID feedbackId);
    long countByDecision(DraftDecision decision);
}
