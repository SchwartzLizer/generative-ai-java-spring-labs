package com.schwartzlizer.support.feedback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Feedback> findByCustomerReferenceContainingIgnoreCaseOrderByCreatedAtDesc(String customerReference, Pageable pageable);
    long countByStatus(FeedbackStatus status);
    long countByStatusIn(Collection<FeedbackStatus> statuses);
    long countByIdIn(Collection<UUID> ids);
}
