package com.schwartzlizer.support.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the read-only operational summary consumed by the dashboard UI and by monitoring, under
 * {@code /api/v1/dashboard}.
 *
 * <p>Delegates to {@link DashboardService}.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service=service; }
    /**
     * Returns the current feedback, analysis and draft counters.
     *
     * <p>All counters are read in one read-only transaction, so the snapshot is internally consistent; values
     * are live, not cached.
     *
     * @return the dashboard counter snapshot
     */
    @GetMapping("/summary") public DashboardSummary summary() { return service.summary(); }
}
