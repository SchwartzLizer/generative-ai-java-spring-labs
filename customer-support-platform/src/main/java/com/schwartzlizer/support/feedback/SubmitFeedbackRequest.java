package com.schwartzlizer.support.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitFeedbackRequest(@NotBlank @Size(min=1, max=100) String customerReference, @NotBlank @Size(min=1, max=4000) String message) { }
