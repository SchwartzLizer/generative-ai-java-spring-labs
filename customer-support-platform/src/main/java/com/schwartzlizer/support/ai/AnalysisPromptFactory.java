package com.schwartzlizer.support.ai;

import com.schwartzlizer.support.analysis.*;
import org.springframework.stereotype.Component;

@Component
public class AnalysisPromptFactory {
    public String create(String message) {
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Feedback message is required");
        return "Analyze this synthetic customer feedback. Return JSON only with exactly these keys: sentiment, category, urgency, recommendedAction. Allowed sentiment values: POSITIVE, NEUTRAL, NEGATIVE. Allowed category values: SECURITY, BILLING, TECHNICAL, DELIVERY, GENERAL. Allowed urgency values: HIGH, MEDIUM, LOW. Do not include markdown. Do not invent customer facts.\nFeedback:\n" + message.trim();
    }
}
