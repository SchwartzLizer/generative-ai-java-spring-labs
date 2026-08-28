package com.schwartzlizer.ai.response;

import java.util.Objects;

public final class GeminiSupportResponseAiClient implements SupportResponseAiClient {
    private final ChatCompletionGateway gateway;

    public GeminiSupportResponseAiClient(ChatCompletionGateway gateway) {
        this.gateway = Objects.requireNonNull(gateway, "Chat gateway is required");
    }

    @Override
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }
        var response = gateway.complete(prompt);
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI provider returned an empty response");
        }
        return response.trim();
    }
}
