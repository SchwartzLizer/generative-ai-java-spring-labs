package com.schwartzlizer.support.feedback;

import com.schwartzlizer.support.analysis.FeedbackAnalysisRepository;
import com.schwartzlizer.support.response.ResponseDraftRepository;
import com.schwartzlizer.support.common.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Application service for the feedback lifecycle: submission, paged listing, single-item reads with analysis and
 * draft history, and agent-driven status changes.
 *
 * <p>Transaction boundaries live here: write methods are {@code @Transactional} and read methods are
 * {@code @Transactional(readOnly = true)}. Transition rules are not enforced here; they belong to
 * {@link Feedback}.
 *
 * <p>UUIDs and timestamps come from an injected {@code Supplier<UUID>} and {@code Clock}, so results are
 * deterministic under test.
 */
@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackAnalysisRepository analysisRepository;
    private final ResponseDraftRepository draftRepository;
    private final Supplier<UUID> uuidSupplier;
    private final Clock clock;
    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, FeedbackAnalysisRepository analysisRepository, ResponseDraftRepository draftRepository, Supplier<UUID> uuidSupplier, Clock clock) {
        this.feedbackRepository=feedbackRepository;
        this.analysisRepository=analysisRepository;
        this.draftRepository=draftRepository;
        this.uuidSupplier=uuidSupplier;
        this.clock=clock;
    }
    /**
     * Creates a service limited to feedback storage, without analysis or draft access.
     *
     * <p>The analysis and draft repositories are left null, so {@code get(UUID)} always returns empty analyses
     * and drafts lists for instances built this way. Intended for narrow unit tests; application code must use
     * the primary, injected constructor.
     *
     * @param feedbackRepository storage for feedback aggregates
     * @param uuidSupplier source of identifiers for new feedback
     * @param clock source of creation and update timestamps
     */
    public FeedbackService(FeedbackRepository feedbackRepository, Supplier<UUID> uuidSupplier, Clock clock) { this(feedbackRepository, null, null, uuidSupplier, clock); }
    /**
     * Persists a new feedback item in status {@code NEW}.
     *
     * @param request validated submission payload carrying customerReference and message
     * @return a basic view of the stored feedback, with empty analyses and drafts lists
     * @throws IllegalArgumentException if the request fields fail the {@code Feedback.create} invariants (null
     *         or blank); surfaces as HTTP 400
     */
    @Transactional
    public FeedbackResponse submit(SubmitFeedbackRequest request) {
        Feedback saved=feedbackRepository.save(Feedback.create(uuidSupplier.get(), request.customerReference(), request.message(), Instant.now(clock)));
        return FeedbackResponse.basic(saved);
    }
    /**
     * Returns a page of feedback items, newest first.
     *
     * <p>Ordering is fixed to {@code createdAt} descending by the repository query; any sort supplied on the
     * {@code Pageable} is ignored. Entries carry no analyses or drafts.
     *
     * @param pageable page number and size; the sort attribute has no effect
     * @return the requested page of basic feedback views
     */
    @Transactional(readOnly=true)
    public Page<FeedbackResponse> list(Pageable pageable) { return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable).map(FeedbackResponse::basic); }
    /**
     * Returns a page of feedback items filtered by customer reference, newest first.
     *
     * <p>A null or blank {@code query} returns the unfiltered page; otherwise the trimmed query is matched
     * case-insensitively as a substring of {@code customerReference}. Same fixed ordering and same sort caveat
     * as the single-argument overload.
     *
     * @param query optional customer-reference fragment; null or blank means no filter
     * @param pageable page number and size; the sort attribute has no effect
     * @return the matching page of basic feedback views
     */
    @Transactional(readOnly=true)
    public Page<FeedbackResponse> list(String query, Pageable pageable) { return (query == null || query.isBlank() ? feedbackRepository.findAllByOrderByCreatedAtDesc(pageable) : feedbackRepository.findByCustomerReferenceContainingIgnoreCaseOrderByCreatedAtDesc(query.trim(), pageable)).map(FeedbackResponse::basic); }
    /**
     * Returns one feedback item together with its full analysis and draft history.
     *
     * <p>Analyses and drafts are both ordered by {@code createdAt} ascending. If this service was built with the
     * narrow constructor above, both lists are always empty.
     *
     * @param id identifier of the feedback item
     * @return the detailed view
     * @throws ResourceNotFoundException if no feedback exists with that id; surfaces as HTTP 404
     */
    @Transactional(readOnly=true)
    public FeedbackResponse get(UUID id) {
        Feedback feedback=find(id);
        var analyses=analysisRepository == null ? java.util.List.<com.schwartzlizer.support.analysis.FeedbackAnalysisResponse>of() : analysisRepository.findByFeedback_IdOrderByCreatedAtAsc(id).stream().map(com.schwartzlizer.support.analysis.FeedbackAnalysisResponse::from).toList();
        var drafts=draftRepository == null ? java.util.List.<com.schwartzlizer.support.response.ResponseDraftResponse>of() : draftRepository.findByFeedback_IdOrderByCreatedAtAsc(id).stream().map(com.schwartzlizer.support.response.ResponseDraftResponse::from).toList();
        return new FeedbackResponse(feedback.id(),feedback.customerReference(),feedback.message(),feedback.status(),feedback.createdAt(),feedback.updatedAt(),analyses,drafts);
    }
    /**
     * Applies an agent-requested status transition to one feedback item.
     *
     * <p>The legal-transition rule is enforced by {@link Feedback#changeStatus}; refer to it rather than
     * repeating the table here.
     *
     * @param id identifier of the feedback item
     * @param status requested target status
     * @return a basic view of the updated feedback
     * @throws ResourceNotFoundException if no feedback exists with that id; surfaces as HTTP 404
     * @throws com.schwartzlizer.support.common.InvalidStateTransitionException if the transition is not
     *         permitted from the current status; surfaces as HTTP 409
     */
    @Transactional
    public FeedbackResponse changeStatus(UUID id, FeedbackStatus status) {
        Feedback feedback=find(id);
        feedback.changeStatus(status, Instant.now(clock));
        return FeedbackResponse.basic(feedbackRepository.save(feedback));
    }
    /**
     * Loads the feedback aggregate for use by callers inside an existing transaction.
     *
     * <p>Returns the managed entity rather than a view. This method itself declares no transaction; a caller
     * intending to mutate the result must supply its own transactional boundary.
     *
     * @param id identifier of the feedback item
     * @return the managed aggregate
     * @throws ResourceNotFoundException if no feedback exists with that id; surfaces as HTTP 404
     */
    public Feedback find(UUID id) { return feedbackRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Feedback was not found")); }
}
