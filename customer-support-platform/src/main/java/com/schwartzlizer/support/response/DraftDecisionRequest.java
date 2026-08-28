package com.schwartzlizer.support.response;

import jakarta.validation.constraints.NotNull;

public record DraftDecisionRequest(@NotNull DraftDecision decision) { }
