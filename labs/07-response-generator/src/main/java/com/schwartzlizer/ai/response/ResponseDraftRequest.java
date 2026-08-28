package com.schwartzlizer.ai.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResponseDraftRequest(
        @NotBlank @Size(max = 2000) String issue,
        @NotBlank @Pattern(regexp = "professional|empathetic|concise") String tone) {
}
