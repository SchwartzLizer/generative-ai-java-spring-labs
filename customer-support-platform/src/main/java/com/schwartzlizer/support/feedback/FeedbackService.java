package com.schwartzlizer.support.feedback;

import com.schwartzlizer.support.analysis.FeedbackAnalysisRepository;
import com.schwartzlizer.support.common.OptimisticLockingConflictException;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import com.schwartzlizer.support.response.ResponseDraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class FeedbackService {
    private static final int MAX_STATUS_CHANGE_ATTEMPTS = 3;

    private final FeedbackRepository feedbackRepository;
    private final FeedbackAnalysisRepository analysisRepository;
    private final ResponseDraftRepository draftRepository;
    private final Supplier<UUID> uuidSupplier;
    private final Clock clock;
    private final FeedbackStatusChangeTxOperations statusChangeTxOperations;

    @Autowired
    public FeedbackService(
        FeedbackRepository feedbackRepository,
        FeedbackAnalysisRepository analysisRepository,
        ResponseDraftRepository draftRepository,
        Supplier<UUID> uuidSupplier,
        Clock clock,
        FeedbackStatusChangeTxOperations statusChangeTxOperations
    ) {
        this.feedbackRepository = feedbackRepository;
        this.analysisRepository = analysisRepository;
        this.draftRepository = draftRepository;
        this.uuidSupplier = uuidSupplier;
        this.clock = clock;
        this.statusChangeTxOperations = statusChangeTxOperations;
    }

    public FeedbackService(
        FeedbackRepository feedbackRepository,
        FeedbackAnalysisRepository analysisRepository,
        ResponseDraftRepository draftRepository,
        Supplier<UUID> uuidSupplier,
        Clock clock
    ) {
        this(feedbackRepository, analysisRepository, draftRepository, uuidSupplier, clock, null);
    }

    public FeedbackService(FeedbackRepository feedbackRepository, Supplier<UUID> uuidSupplier, Clock clock) {
        this(feedbackRepository, null, null, uuidSupplier, clock, null);
    }

    @Transactional
    public FeedbackResponse submit(SubmitFeedbackRequest request) {
        Feedback saved = feedbackRepository.save(
            Feedback.create(uuidSupplier.get(), request.customerReference(), request.message(), Instant.now(clock)));
        return FeedbackResponse.basic(saved);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> list(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable).map(FeedbackResponse::basic);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> list(String query, Pageable pageable) {
        return (query == null || query.isBlank()
            ? feedbackRepository.findAllByOrderByCreatedAtDesc(pageable)
            : feedbackRepository.findByCustomerReferenceContainingIgnoreCaseOrderByCreatedAtDesc(query.trim(), pageable))
            .map(FeedbackResponse::basic);
    }

    @Transactional(readOnly = true)
    public FeedbackResponse get(UUID id) {
        Feedback feedback = find(id);
        var analyses = analysisRepository == null
            ? java.util.List.<com.schwartzlizer.support.analysis.FeedbackAnalysisResponse>of()
            : analysisRepository.findByFeedback_IdOrderByCreatedAtAsc(id).stream()
                .map(com.schwartzlizer.support.analysis.FeedbackAnalysisResponse::from).toList();
        var drafts = draftRepository == null
            ? java.util.List.<com.schwartzlizer.support.response.ResponseDraftResponse>of()
            : draftRepository.findByFeedback_IdOrderByCreatedAtAsc(id).stream()
                .map(com.schwartzlizer.support.response.ResponseDraftResponse::from).toList();
        return new FeedbackResponse(
            feedback.id(), feedback.customerReference(), feedback.message(), feedback.status(),
            feedback.createdAt(), feedback.updatedAt(), analyses, drafts);
    }

    public FeedbackResponse changeStatus(UUID id, FeedbackStatus status) {
        if (statusChangeTxOperations == null) {
            return changeStatusInCurrentService(id, status);
        }
        for (int attempt = 1; attempt <= MAX_STATUS_CHANGE_ATTEMPTS; attempt++) {
            try {
                return statusChangeTxOperations.changeStatus(id, status);
            } catch (ObjectOptimisticLockingFailureException exception) {
                if (attempt == MAX_STATUS_CHANGE_ATTEMPTS) {
                    throw optimisticConflict(exception);
                }
            } catch (OptimisticLockingFailureException exception) {
                if (attempt == MAX_STATUS_CHANGE_ATTEMPTS) {
                    throw optimisticConflict(exception);
                }
            }
        }
        throw new IllegalStateException("Unreachable retry state");
    }

    @Transactional
    protected FeedbackResponse changeStatusInCurrentService(UUID id, FeedbackStatus status) {
        Feedback feedback = find(id);
        feedback.changeStatus(status, Instant.now(clock));
        return FeedbackResponse.basic(feedbackRepository.save(feedback));
    }

    private OptimisticLockingConflictException optimisticConflict(Throwable cause) {
        return new OptimisticLockingConflictException(
            "Feedback status could not be changed because it was modified concurrently", cause);
    }

    public Feedback find(UUID id) {
        return feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback was not found"));
    }
}
