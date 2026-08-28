package com.schwartzlizer.ai.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class ChatService {
    private final CustomerSupportAiClient client;
    private final String provider;

    public ChatService(CustomerSupportAiClient client, @Value("${app.ai.provider:demo}") String provider) {
        this.client = Objects.requireNonNull(client, "AI client is required");
        this.provider = Objects.requireNonNull(provider, "AI provider is required");
    }

    public ChatResponse reply(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        var reply = client.reply(message.trim());
        if (reply == null || reply.isBlank()) {
            throw new IllegalStateException("AI provider returned an empty response");
        }
        return new ChatResponse(reply.trim(), provider);
    }
}
