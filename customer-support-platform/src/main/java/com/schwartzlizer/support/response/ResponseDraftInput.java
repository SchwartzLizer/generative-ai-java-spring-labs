package com.schwartzlizer.support.response;

import com.schwartzlizer.support.ai.FeedbackAnalysisResult;

import java.util.UUID;

record ResponseDraftInput(UUID feedbackId, String message, FeedbackAnalysisResult analysis) {
}
