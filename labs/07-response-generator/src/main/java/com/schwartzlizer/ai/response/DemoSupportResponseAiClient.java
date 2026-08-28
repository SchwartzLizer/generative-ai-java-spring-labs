package com.schwartzlizer.ai.response;

public final class DemoSupportResponseAiClient implements SupportResponseAiClient {
    @Override
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }
        return "Thanks for letting us know. We are sorry about the issue. "
                + "Our team will review the details and share the next safe step through the supported channel.";
    }
}
