package com.schwartzlizer.ai.response;

import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public final class ResponsePromptFactory {
    private static final Set<String> ALLOWED_TONES = Set.of("professional", "empathetic", "concise");

    public String create(String issue, String tone) {
        if (issue == null || issue.isBlank()) {
            throw new IllegalArgumentException("Issue is required");
        }
        if (issue.length() > 2000) {
            throw new IllegalArgumentException("Issue must be at most 2000 characters");
        }
        if (tone == null || !ALLOWED_TONES.contains(tone.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Tone must be professional, empathetic, or concise");
        }
        var normalizedTone = tone.toLowerCase(Locale.ROOT);
        return "Draft a " + normalizedTone + " customer support response for the issue below. "
                + "Do not promise refunds or dates. Do not invent account details or policies. "
                + "Acknowledge uncertainty and suggest a safe next step.\n\nIssue:\n" + issue.trim();
    }
}
