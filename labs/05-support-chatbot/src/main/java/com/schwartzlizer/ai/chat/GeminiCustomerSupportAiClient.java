package com.schwartzlizer.ai.chat;

import java.util.Objects;

public final class GeminiCustomerSupportAiClient implements CustomerSupportAiClient {
    private final ChatCompletionGateway gateway;

    public GeminiCustomerSupportAiClient(ChatCompletionGateway gateway) {
        this.gateway = Objects.requireNonNull(gateway, "Chat gateway is required");
    }

    @Override
    public String reply(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        var prompt = "You are a customer support assistant. Respond clearly and safely to the customer message below. "
                + "Do not invent account details, policies, refunds, or dates. Ask for only relevant non-sensitive details.\n\n"
                + "Customer message:\n" + message.trim();
        var response = gateway.complete(prompt);
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI provider returned an empty response");
        }
        return response.trim();
    }
}
