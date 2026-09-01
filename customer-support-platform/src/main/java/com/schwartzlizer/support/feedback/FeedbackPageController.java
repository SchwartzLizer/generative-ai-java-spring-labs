package com.schwartzlizer.support.feedback;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@Controller
public class FeedbackPageController {
    private final FeedbackService service;
    public FeedbackPageController(FeedbackService service) { this.service=service; }
    @GetMapping("/feedback/{id}")
    public String detail(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("feedback", service.get(id));
        return "feedback-detail";
    }
}
