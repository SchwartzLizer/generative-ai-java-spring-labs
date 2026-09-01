package com.schwartzlizer.support.dashboard;

import com.schwartzlizer.support.feedback.FeedbackService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardPageController {
    private final DashboardService dashboardService;
    private final FeedbackService feedbackService;
    public DashboardPageController(DashboardService dashboardService, FeedbackService feedbackService) {
        this.dashboardService=dashboardService;
        this.feedbackService=feedbackService;
    }
    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(name="query", defaultValue="") String query, Model model) {
        model.addAttribute("summary", dashboardService.summary());
        model.addAttribute("query", query);
        model.addAttribute("feedbackPage", feedbackService.list(query, PageRequest.of(0,20, Sort.by(Sort.Direction.DESC,"createdAt"))));
        return "dashboard";
    }
}
