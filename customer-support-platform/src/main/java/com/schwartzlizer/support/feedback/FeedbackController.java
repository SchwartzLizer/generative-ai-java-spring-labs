package com.schwartzlizer.support.feedback;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

/**
 * REST entry point for the feedback lifecycle under {@code /api/v1/feedback}.
 *
 * <p>Holds no business logic and delegates to {@link FeedbackService}. All errors are rendered as the shared
 * {@code ApiError} payload by {@link com.schwartzlizer.support.common.GlobalExceptionHandler}.
 *
 * <p>The API requires an authenticated {@code AGENT} or {@code ADMIN} role.
 */
@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {
    private final FeedbackService service;
    public FeedbackController(FeedbackService service) { this.service=service; }
    /**
     * Accepts a new customer feedback submission.
     *
     * <p>Responds 201 Created with a {@code Location} header of {@code /api/v1/feedback/{id}}.
     *
     * @param request validated submission payload
     * @return 201 Created with the stored feedback in the body
     * @throws IllegalArgumentException if the payload violates the domain invariants; surfaces as HTTP 400
     */
    @PostMapping
    public ResponseEntity<FeedbackResponse> submit(@Valid @RequestBody SubmitFeedbackRequest request) {
        FeedbackResponse response=service.submit(request);
        return ResponseEntity.created(URI.create("/api/v1/feedback/"+response.id())).body(response);
    }
    /**
     * Returns a page of feedback items, newest first.
     *
     * <p>Default page size is 20; results are always ordered by {@code createdAt} descending and a
     * client-supplied sort parameter is not applied.
     *
     * @param pageable page and size from the request query string
     * @return the requested page of feedback views
     */
    @GetMapping
    public Page<FeedbackResponse> list(@PageableDefault(size=20, sort="createdAt") Pageable pageable) { return service.list(pageable); }
    /**
     * Returns one feedback item with its analysis and draft history.
     *
     * @param id path identifier of the feedback item
     * @return the detailed feedback view
     * @throws com.schwartzlizer.support.common.ResourceNotFoundException if no feedback exists with that id;
     *         surfaces as HTTP 404
     */
    @GetMapping("/{id}")
    public FeedbackResponse get(@PathVariable("id") UUID id) { return service.get(id); }
    /**
     * Applies a status transition to one feedback item.
     *
     * @param id path identifier of the feedback item
     * @param request validated payload carrying the requested target status
     * @return the updated feedback view
     * @throws com.schwartzlizer.support.common.ResourceNotFoundException if no feedback exists with that id;
     *         surfaces as HTTP 404
     * @throws com.schwartzlizer.support.common.InvalidStateTransitionException if the transition is not
     *         permitted from the current status; surfaces as HTTP 409
     */
    @PatchMapping("/{id}/status")
    public FeedbackResponse changeStatus(@PathVariable("id") UUID id, @Valid @RequestBody UpdateFeedbackStatusRequest request) { return service.changeStatus(id, request.status()); }
}
