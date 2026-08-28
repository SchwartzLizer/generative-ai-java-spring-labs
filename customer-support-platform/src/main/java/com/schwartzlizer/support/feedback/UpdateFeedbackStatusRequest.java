package com.schwartzlizer.support.feedback;

import jakarta.validation.constraints.NotNull;

public record UpdateFeedbackStatusRequest(@NotNull FeedbackStatus status) { }
