package com.schwartzlizer.ai.response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class ResponseDraftService {
    private final ResponsePromptFactory promptFactory;
    private final SupportResponseAiClient client;
    private final String provider;

    public ResponseDraftService(ResponsePromptFactory promptFactory, SupportResponseAiClient client,
                                @Value("${app.ai.provider:demo}") String provider) {
        this.promptFactory = Objects.requireNonNull(promptFactory, "Prompt factory is required");
        this.client = Objects.requireNonNull(client, "AI client is required");
        this.provider = Objects.requireNonNull(provider, "AI provider is required");
    }

    public ResponseDraft create(String issue, String tone) {
        var prompt = promptFactory.create(issue, tone);
        var content = client.generate(prompt);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI provider returned an empty response");
        }
        return new ResponseDraft(content.trim(), provider);
    }
}
